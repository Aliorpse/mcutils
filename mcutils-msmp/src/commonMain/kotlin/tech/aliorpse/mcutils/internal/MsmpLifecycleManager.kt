package tech.aliorpse.mcutils.internal

import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tech.aliorpse.mcutils.api.MsmpClientConfig
import tech.aliorpse.mcutils.api.MsmpState
import tech.aliorpse.mcutils.entity.ServerStoppingEvent
import tech.aliorpse.mcutils.internal.util.DispatchersIO
import tech.aliorpse.mcutils.internal.util.WebSocketClientProvider.webSocketClient
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

internal class MsmpLifecycleManager(
    private val target: String,
    private val token: String,
    private val config: MsmpClientConfig,
    private val scope: CoroutineScope
) {
    private val _stateFlow = MutableStateFlow<MsmpState>(MsmpState.Disconnected(null))

    val stateFlow = _stateFlow.asStateFlow()

    private val loopMutex = Mutex()

    private var loopJob: Job? = null

    fun start() {
        scope.launch {
            loopMutex.withLock {
                if (loopJob?.isActive == true) return@launch
                loopJob = launch(DispatchersIO) {
                    runConnectionLoop()
                }
            }
        }
    }

    suspend fun close() {
        loopMutex.withLock {
            loopJob?.cancelAndJoin()

            val currentState = _stateFlow.value
            _stateFlow.value = MsmpState.Closed

            if (currentState is MsmpState.Connected) {
                withContext(NonCancellable) {
                    currentState.connection.closeConnection()
                }
            }
        }
    }

    suspend fun awaitConnection(): MsmpConnection {
        val state = stateFlow
            .filter {
                it is MsmpState.Connected ||
                    it is MsmpState.Closed ||
                    (it is MsmpState.Disconnected && !config.autoReconnect)
            }
            .first()

        return when (state) {
            is MsmpState.Connected -> state.connection
            is MsmpState.Closed -> error("Client closed while awaiting connection.")
            is MsmpState.Disconnected -> error("Client disconnected without auto-reconnect.")
            else -> error("Unexpected state: $state")
        }
    }

    private var hasConnectedOnce = false
    private var connectionAttempts = 0

    private suspend fun runConnectionLoop() {
        connectionAttempts = 0
        hasConnectedOnce = false

        while (currentCoroutineContext().isActive) {
            _stateFlow.value = MsmpState.Connecting

            runCatching {
                val shouldStop = connectSession()

                if (shouldStop) {
                    _stateFlow.value = MsmpState.Closed
                    return
                } else {
                    error("Connection closed unexpectedly")
                }
            }.onFailure { e ->
                if (e is CancellationException) throw e

                _stateFlow.value = MsmpState.Disconnected(e)

                @Suppress("ComplexCondition")
                if (
                    !config.autoReconnect ||
                    isMaxRetryReached(connectionAttempts) ||
                    (config.failFast && !hasConnectedOnce)
                ) {
                    _stateFlow.value = MsmpState.Closed
                    return
                }

                val delayDuration = calculateBackoff(
                    connectionAttempts++,
                    config.initialRetryDelay,
                    config.maxRetryDelay
                )

                _stateFlow.value = MsmpState.Reconnecting(connectionAttempts.toLong(), delayDuration)

                delay(delayDuration)
            }
        }
    }

    /**
     * @return true means connection should close; false means need reconnection
     */
    private suspend fun connectSession(): Boolean = coroutineScope {
        val session = webSocketClient.webSocketSession(target) {
            header(HttpHeaders.Authorization, "Bearer $token")
            timeout { connectTimeoutMillis = config.connectTimeout }
        }

        hasConnectedOnce = true
        connectionAttempts = 0

        val connection = MsmpConnection(session, config.eventBufferSize, config.batchDelay)

        // Register listener for server stopping event
        var isServerStopping = false
        val stopListenerJob = connection.eventFlow
            .filterIsInstance<ServerStoppingEvent>()
            .take(1)
            .onEach {
                isServerStopping = true
                connection.retryOnDisconnected = false
                connection.listeningJob.cancel()
            }
            .launchIn(this)

        _stateFlow.value = MsmpState.Connected(connection)

        connection.listeningJob.join()
        stopListenerJob.cancel()
        isServerStopping
    }

    private fun isMaxRetryReached(attempt: Int): Boolean =
        config.maxReconnectAttempts != -1L && attempt >= config.maxReconnectAttempts

    @Suppress("MagicNumber")
    private fun calculateBackoff(attempt: Int, initial: Long, max: Long): Long {
        val exp = 2.0.pow(min(attempt, 10))
        val base = (initial * exp).toLong().coerceAtMost(max)
        val jitter = Random.nextLong(0, 500)
        return (base + jitter).coerceAtMost(max)
    }
}
