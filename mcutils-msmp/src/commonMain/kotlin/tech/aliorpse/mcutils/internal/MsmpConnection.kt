package tech.aliorpse.mcutils.internal

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import tech.aliorpse.mcutils.entity.ConnectionClosedEvent
import tech.aliorpse.mcutils.entity.ConnectionEstablishedEvent
import tech.aliorpse.mcutils.internal.util.DispatchersIO
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
internal class MsmpConnection internal constructor(
    internal val connection: DefaultClientWebSocketSession,
    eventBufferSize: Int,
    batchDelay: Long
) {
    private val scope = CoroutineScope(DispatchersIO + SupervisorJob())

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val responseHandler = MsmpResponseHandler(json, eventBufferSize)

    private val messageSender = MsmpRequestSender(connection, json, batchDelay, scope)

    val eventFlow = responseHandler.eventFlow

    internal var retryOnDisconnected: Boolean = true

    val listeningJob: Job = scope.launch {
        runCatching {
            responseHandler.eventFlow.emit(ConnectionEstablishedEvent)

            connection.incoming.receiveAsFlow()
                .filterIsInstance<Frame.Text>()
                .collect { frame ->
                    val text = frame.readText()
                    val element = json.parseToJsonElement(text)
                    responseHandler.handleIncomingElement(element)
                }
        }.also { result ->
            handleConnectionClosure(result)
        }
    }

    init {
        messageSender.loopJob.invokeOnCompletion { cause ->
            if (cause != null && cause !is CancellationException) {
                scope.launch { closeConnection() }
            }
        }
    }

    @PublishedApi
    internal suspend fun call(
        method: String,
        paramElement: JsonElement,
    ): JsonElement {
        val (request, deferred) = responseHandler.registerRequest(method, paramElement)

        return runCatching {
            messageSender.send(request)
            deferred.await()
        }.onFailure { e ->
            if (deferred.isActive) {
                responseHandler.cancelRequest(request.id, e)
            }
        }.getOrThrow()
    }

    internal suspend fun closeConnection() {
        try {
            connection.close()
        } finally {
            cleanup("Client closed client manually")
        }
    }

    private suspend fun handleConnectionClosure(result: Result<Unit>) {
        withContext(NonCancellable) {
            val closeReason = runCatching { connection.closeReason.await() }.getOrNull()
            val exception = result.exceptionOrNull()

            val finalReason = closeReason?.message
                ?: exception?.message
                ?: exception?.toString()
                ?: "Connection closed (unknown reason)"

            eventFlow.emit(ConnectionClosedEvent(finalReason, retryOnDisconnected))
            scope.launch { connection.close() }

            cleanup(finalReason)
        }
    }

    private fun cleanup(reason: String) {
        messageSender.close()
        responseHandler.cleanupPendingRequests(reason)
        scope.cancel()
    }
}
