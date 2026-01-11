package tech.aliorpse.mcutils.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.serializer
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.entity.MsmpEvent
import tech.aliorpse.mcutils.internal.MsmpConnection
import tech.aliorpse.mcutils.internal.MsmpLifecycleManager
import tech.aliorpse.mcutils.internal.util.AtomicMutableMap
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
    target: String,
    token: String,
    @PublishedApi internal val config: MsmpClientConfig
) : AutoCloseable {
    @PublishedApi
    internal val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val lifecycleManager = MsmpLifecycleManager(target, token, config, scope)

    @PublishedApi
    internal val callExtensions: AtomicMutableMap<String, Any> = AtomicMutableMap()

    @PublishedApi
    internal suspend fun awaitCall(
        method: String,
        params: JsonElement,
        totalTimeout: Long,
    ): JsonElement {
        check(stateFlow.value !is MsmpState.Closed) { "Client already closed, but with pending requests not sent." }
        return withTimeout(totalTimeout) {
            lifecycleManager.awaitConnection().call(method, params)
        }
    }

    @PublishedApi
    internal fun <R> JsonElement.decodeBy(
        responseSerializer: KSerializer<R>
    ): R = json.decodeFromJsonElement(responseSerializer, this)

    @PublishedApi
    internal val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    // -- Universal API --

    public val stateFlow: StateFlow<MsmpState> get() = lifecycleManager.stateFlow

    public fun connect(): Unit = lifecycleManager.start()

    override fun close() {
        lifecycleManager.close()
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
    public suspend inline fun <reified R> call(
        method: String,
        timeout: Long = config.requestTimeout
    ): R = awaitCall(method, JsonArray(emptyList()), timeout).decodeBy(serializer<R>())

    /**
     * Sends a request with parameters using automatic serialization.
     *
     * **Suspends** if the client is connecting or reconnecting; it waits for a valid connection.
     *
     * @throws IllegalStateException if the client is [MsmpState.Closed].
     * @throws TimeoutCancellationException if no response is received within [timeout].
     */
    public suspend inline fun <reified P, reified R> call(
        method: String,
        params: P,
        timeout: Long = config.requestTimeout
    ): R = awaitCall(method, json.encodeToJsonElement(params), timeout).decodeBy(serializer<R>())

    /**
     * Sends a request with parameters using a manual serializer.
     *
     * Useful for types where standard reified type inference might fail (e.g., generic collections).
     *
     * **Suspends** if the client is connecting or reconnecting; it waits for a valid connection.
     *
     * @throws IllegalStateException if the client is [MsmpState.Closed].
     * @throws TimeoutCancellationException if no response is received within [timeout].
     */
    public suspend fun <R> call(
        method: String,
        responseSerializer: KSerializer<R>,
        timeout: Long = config.requestTimeout
    ): R = awaitCall(method, JsonArray(emptyList()), timeout).decodeBy(responseSerializer)

    /**
     * Sends a request with parameters using a manual serializer.
     *
     * Useful for types where standard reified type inference might fail (e.g., generic collections).
     *
     * **Suspends** if the client is connecting or reconnecting; it waits for a valid connection.
     *
     * @throws IllegalStateException if the client is [MsmpState.Closed].
     * @throws TimeoutCancellationException if no response is received within [timeout].
     */
    public suspend inline fun <P, R> call(
        method: String,
        params: P,
        paramSerializer: KSerializer<P>,
        responseSerializer: KSerializer<R>,
        timeout: Long = config.requestTimeout
    ): R = awaitCall(method, json.encodeToJsonElement(paramSerializer, params), timeout).decodeBy(responseSerializer)

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
