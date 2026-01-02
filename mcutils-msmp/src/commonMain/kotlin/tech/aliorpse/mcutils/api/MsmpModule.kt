package tech.aliorpse.mcutils.api

import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.entity.MsmpEvent
import tech.aliorpse.mcutils.entity.ServerStoppingEvent
import tech.aliorpse.mcutils.internal.impl.MsmpConnection
import tech.aliorpse.mcutils.internal.util.AtomicCopyOnWriteMap
import tech.aliorpse.mcutils.internal.util.DispatchersIO
import tech.aliorpse.mcutils.internal.util.WebSocketClientProvider.webSocketClient
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Suppress("MagicNumber")
public class MsmpClientConfig {
    /**
     * Whether to automatically reconnect when the connection is lost.
     */
    public var autoReconnect: Boolean = true

    /**
     * Connection timeout in milliseconds.
     */
    public var connectTimeout: Long = 3000L

    /**
     * Default request timeout in milliseconds. (maybe override by extensions)
     */
    public var requestTimeout: Long = 10000L

    /**
     * Internal buffer size for events.
     */
    public var eventBufferSize: Int = 64

    /**
     * If true, the client will not attempt to reconnect if the first connection attempt fails.
     */
    public var failFast: Boolean = true

    /**
     * Maximum number of reconnection attempts. -1 for infinite.
     */
    public var maxReconnectAttempts: Long = -1L

    /**
     * The initial delay for the exponential backoff in milliseconds.
     */
    public var initialRetryDelay: Long = 1000L

    /**
     * The maximum delay between reconnection attempts in milliseconds.
     */
    public var maxRetryDelay: Long = 30000L

    /**
     * If > 0, requests occurring within this time window will be grouped into a single JSON-RPC Batch request.
     * Set to 0 to disable batching (send immediately).
     */
    public var batchDelay: Long = 30L
}

@ExperimentalMCUtilsApi
public fun MCServer.msmpClient(
    target: String,
    token: String,
    config: MsmpClientConfig.() -> Unit = {}
): MsmpClient = MsmpClient(target, token, MsmpClientConfig().apply(config))

@OptIn(ExperimentalAtomicApi::class)
@Suppress("TooManyFunctions")
public class MsmpClient internal constructor(
    private val target: String,
    private val token: String,
    @PublishedApi internal val config: MsmpClientConfig
) : AutoCloseable {
    @PublishedApi
    internal val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _state = MutableStateFlow<MsmpState>(MsmpState.Disconnected(null))
    public val stateFlow: StateFlow<MsmpState> = _state.asStateFlow()

    private var connectJob = AtomicReference<Job?>(null)

    @PublishedApi
    internal val callExtensions: AtomicCopyOnWriteMap<String, Any> = AtomicCopyOnWriteMap()

    internal suspend fun awaitConnection(): MsmpConnection =
        stateFlow.transform { s ->
            when (s) {
                is MsmpState.Connected -> emit(s.connection)
                is MsmpState.Closed ->
                    error("MsmpClient was closed while waiting for connection, with pending requests not sent.")
                is MsmpState.Disconnected if !config.autoReconnect ->
                    error("Client disconnected with no auto reconnect enabled, with pending requests not sent.")
                else -> {}
            }
        }.first()

    @PublishedApi
    internal suspend fun awaitCall(method: String, params: JsonElement, totalTimeout: Long): JsonElement {
        // It's the user's fault calling this function when the client closed
        check(stateFlow.value !is MsmpState.Closed) { "Client already closed, but there are still pending requests." }

        return withTimeout(totalTimeout) { awaitConnection().call(method, params) }
    }

    // -- Universal API --

    /**
     * Default for serialization.
     */
    public val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Start connection to the server.
     */
    public fun startConnection() {
        // Use a local flag to track if we should stop completely
        connectJob.exchange(null)?.cancel()

        val newJob = scope.launch(DispatchersIO) {
            var isServerStopping = false
            var attempt = 0L

            try {
                while (isActive) {
                    _state.value = MsmpState.Connecting

                    // Use runCatching specifically for the connection process
                    val result = runCatching {
                        doConnect {
                            isServerStopping = true
                        }
                    }

                    if (result.isSuccess) {
                        attempt = 0 // Reset on success
                    } else {
                        val error = result.exceptionOrNull()
                        _state.value = MsmpState.Disconnected(error)

                        // Fail-fast: only on the very first attempt
                        if (config.failFast && attempt == 0L) {
                            isServerStopping = true
                        }
                    }

                    // Centralized exit check
                    val maxReached = config.maxReconnectAttempts != -1L && attempt >= config.maxReconnectAttempts

                    if (isServerStopping || !config.autoReconnect || !isActive || maxReached) {
                        // Ensure the state is set to Closed if we are not reconnecting anymore
                        _state.value = MsmpState.Closed
                        break
                    }

                    // Prepare for reconnection
                    val delayTime = calculateDelay(attempt++, config.initialRetryDelay, config.maxRetryDelay)
                    _state.value = MsmpState.Reconnecting(attempt, delayTime)

                    // Delay could be interrupted by job cancellation
                    delay(delayTime)
                }
            } finally {
                // Guaranteed state transition to Closed on terminal exit
                withContext(NonCancellable) {
                    _state.value = MsmpState.Closed
                    connectJob.compareAndExchange(coroutineContext[Job], null)
                }
            }
        }
        connectJob.store(newJob)
    }

    private suspend fun doConnect(onServerStopping: () -> Unit) {
        val session = webSocketClient.webSocketSession(target) {
            header(HttpHeaders.Authorization, "Bearer $token")
            timeout { connectTimeoutMillis = config.connectTimeout }
        }

        val connection = MsmpConnection(session, config.eventBufferSize, config.batchDelay)
        _state.value = MsmpState.Connected(connection)

        // Monitor server stopping signal
        // We use another scope or supervisor to ensure this collector doesn't fail the whole block
        val stopObserver = connection.eventFlow
            .filterIsInstance<ServerStoppingEvent>()
            .take(1)
            .onEach {
                onServerStopping()
                connection.retryOnDisconnected = false
            }
            .launchIn(scope)

        try {
            connection.listeningJob.join()
        } finally {
            stopObserver.cancel()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun close() {
        connectJob.exchange(null)?.cancel()

        val current = _state.value
        _state.value = MsmpState.Closed

        if (current is MsmpState.Connected) {
            GlobalScope.launch(DispatchersIO) {
                withContext(NonCancellable) {
                    runCatching { current.connection.closeConnection() }
                }
            }
        }

        scope.cancel()
    }

    // --- Request API ---

    /**
     * Sends a request to the server and awaits a response.
     *
     * **Suspends** if the client is connecting or reconnecting; it waits for a valid connection.
     *
     * @throws IllegalStateException if the client is [MsmpState.Closed].
     * @throws TimeoutCancellationException if no response is received within [timeout].
     */
    public suspend inline fun call(
        method: String,
        timeout: Long = config.requestTimeout
    ): JsonElement = awaitCall(method, JsonArray(emptyList()), timeout)

    /**
     * Sends a request with parameters using a manual [serializer].
     *
     * Useful for types where standard reified type inference might fail (e.g., generic collections).
     *
     * **Suspends** if the client is connecting or reconnecting; it waits for a valid connection.
     *
     * @throws IllegalStateException if the client is [MsmpState.Closed].
     * @throws TimeoutCancellationException if no response is received within [timeout].
     */
    public suspend inline fun <reified T> call(
        method: String,
        params: T,
        serializer: KSerializer<T>,
        timeout: Long = config.requestTimeout
    ): JsonElement = awaitCall(method, json.encodeToJsonElement(serializer, params), timeout)

    /**
     * Sends a request with parameters using automatic serialization.
     *
     * **Suspends** if the client is connecting or reconnecting; it waits for a valid connection.
     *
     * @throws IllegalStateException if the client is [MsmpState.Closed].
     * @throws TimeoutCancellationException if no response is received within [timeout].
     */
    public suspend inline fun <reified T> call(
        method: String,
        params: T,
        timeout: Long = config.requestTimeout
    ): JsonElement = awaitCall(method, json.encodeToJsonElement(params), timeout)

    // --- Event API ---

    /**
     * The event flow.
     *
     * This flow emits all events from the underlying connection, filtered by the client's stateFlow.
     * It remains active during reconnection attempts and completes normally when the client stateFlow
     * is [MsmpState.Closed], indicating the server has stopped, or you closed the client.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    public val eventFlow: Flow<MsmpEvent> = stateFlow.takeWhile { it !is MsmpState.Closed }
        .flatMapLatest {
            if (it is MsmpState.Connected) it.connection.eventFlow else emptyFlow()
        }

    /**
     * Returns a [Flow] of events of type [T].
     *
     * This is the preferred way to consume events in reactive environments (like Android).
     * The flow will:
     * 1. Filter events by the specified type [T].
     * 2. Remain active during reconnection attempts.
     * 3. **Complete normally** when the client stateFlow is [MsmpState.Closed], which means server
     * stopped, or you closed the client, thanks to internal stateFlow tracking.
     */
    public inline fun <reified T : MsmpEvent> on(): Flow<T> = eventFlow.filterIsInstance<T>()

    /**
     * Subscribes to events of type [T].
     *
     * **Lifecycle Behavior:**
     * - This listener is strictly bound to the [MsmpClient] lifecycle.
     * - If the connection unexpectedly lost but auto reconnect is true, the collector remains
     * active and will resume receiving events once reconnected.
     *
     * **Note for Android/Lifecycle-sensitive environments:**
     *
     * This method uses the client's internal [scope]. If you need the listener to be canceled
     * when a UI component is destroyed, use the Flow-returning variant instead and collect
     * it within your preferred scope.
     */
    public inline fun <reified T : MsmpEvent> on(
        crossinline block: suspend T.() -> Unit
    ): Job = scope.launch { on<T>().collect { event -> block(event) } }

    /**
     * Awaits the first event of type [T].
     *
     * If the [eventFlow] completes normally (e.g., the client stateFlow is [MsmpState.Closed] or the
     * server stops) before an event is received, this method returns `null`.
     */
    public suspend inline fun <reified T : MsmpEvent> awaitEvent(): T? = eventFlow.filterIsInstance<T>().firstOrNull()

    /**
     * Awaits the first client state of type [T].
     */
    public suspend inline fun <reified T : MsmpState> awaitState(): T? = stateFlow.filterIsInstance<T>().firstOrNull()

    /**
     * Awaits the client to be closed. **Not actually** connection to be closed.
     */
    public suspend inline fun await(): MsmpState.Closed = stateFlow.filterIsInstance<MsmpState.Closed>().first()
}

public sealed class MsmpState {
    @ConsistentCopyVisibility
    public data class Connected internal constructor(internal val connection: MsmpConnection) : MsmpState()
    public data object Connecting : MsmpState()
    public data class Disconnected(val exception: Throwable?) : MsmpState()
    public data class Reconnecting(val attempt: Long, val nextDelay: Long) : MsmpState()
    public data object Closed : MsmpState()
}

@Suppress("MagicNumber")
private fun calculateDelay(attempt: Long, initialDelay: Long, maxDelay: Long): Long {
    val base = (initialDelay shl attempt.toInt().coerceAtMost(10)).coerceAtMost(maxDelay)
    return (base + (0..500).random()).coerceAtMost(maxDelay)
}
