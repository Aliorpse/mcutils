package tech.aliorpse.mcutils.internal.impl

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.entity.ConnectionClosedEvent
import tech.aliorpse.mcutils.entity.ConnectionEstablishedEvent
import tech.aliorpse.mcutils.entity.JsonRpcError
import tech.aliorpse.mcutils.entity.JsonRpcException
import tech.aliorpse.mcutils.entity.MsmpEvent
import tech.aliorpse.mcutils.entity.MsmpEventProvider
import tech.aliorpse.mcutils.entity.MsmpRequest
import tech.aliorpse.mcutils.entity.MsmpResponse
import tech.aliorpse.mcutils.entity.UnknownMsmpEvent
import tech.aliorpse.mcutils.entity.eventMap
import tech.aliorpse.mcutils.entity.fromCode
import tech.aliorpse.mcutils.internal.util.DispatchersIO
import tech.aliorpse.mcutils.internal.util.MutexMutableMap
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

@OptIn(ExperimentalAtomicApi::class)
internal class MsmpConnection internal constructor(
    internal val connection: DefaultClientWebSocketSession,
    eventBufferSize: Int,
    private val batchDelay: Long
) {
    private val scope = CoroutineScope(DispatchersIO + SupervisorJob())

    val eventFlow = MutableSharedFlow<MsmpEvent>(
        extraBufferCapacity = eventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val pendingRequests = MutexMutableMap<Int, CompletableDeferred<JsonElement>>()

    private val idCounter = AtomicInt(0)

    private data class PendingRequestCtx(
        val request: MsmpRequest,
        val deferred: CompletableDeferred<JsonElement>
    )

    private val requestChannel = Channel<PendingRequestCtx>(Channel.UNLIMITED)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val listeningJob: Job
    private val sendingJob: Job

    internal var retryOnDisconnected: Boolean = true

    init {
        listeningJob = scope.launch {
            runCatching {
                eventFlow.emit(ConnectionEstablishedEvent)

                connection.incoming.receiveAsFlow().filterIsInstance<Frame.Text>().collect { frame ->
                    val text = frame.readText()
                    val element = json.parseToJsonElement(text)

                    if (element is JsonArray) {
                        element.forEach { item ->
                            if (item is JsonObject) handleIncomingJson(item)
                        }
                    } else if (element is JsonObject) {
                        handleIncomingJson(element)
                    }
                }
            }.also { result ->
                handleConnectionClosure(result)
            }
        }

        sendingJob = scope.launch {
            runCatching {
                for (firstRequest in requestChannel) {
                    val batch = mutableListOf<PendingRequestCtx>()
                    batch.add(firstRequest)

                    if (batchDelay > 0) {
                        delay(batchDelay)
                        var next = requestChannel.tryReceive().getOrNull()
                        while (next != null) {
                            batch.add(next)
                            next = requestChannel.tryReceive().getOrNull()
                        }
                    } else {
                        var next = requestChannel.tryReceive().getOrNull()
                        while (next != null) {
                            batch.add(next)
                            next = requestChannel.tryReceive().getOrNull()
                        }
                    }

                    sendBatch(batch)
                }
            }.onFailure { e ->
                if (isActive) cancel("Sending job failed", e)
            }
        }
    }

    private suspend fun sendBatch(batch: List<PendingRequestCtx>) {
        if (batch.isEmpty()) return

        runCatching {
            val requests = batch.map { it.request }
            val payload = if (requests.size == 1) {
                json.encodeToString(ListSerializer(MsmpRequest.serializer()), requests)
            } else {
                json.encodeToString(ListSerializer(MsmpRequest.serializer()), requests)
            }

            println(payload)

            connection.send(payload)
        }.onFailure { e ->
            batch.forEach { ctx ->
                pendingRequests.remove(ctx.request.id)
                ctx.deferred.completeExceptionally(e)
            }
        }
    }

    @PublishedApi
    internal suspend fun call(
        method: String,
        paramElement: JsonElement,
    ): JsonElement {
        val id = idCounter.fetchAndIncrement()
        val request = MsmpRequest(id = id, method = method, params = paramElement)
        val deferred = CompletableDeferred<JsonElement>()

        pendingRequests.put(id, deferred)

        runCatching {
            requestChannel.send(PendingRequestCtx(request, deferred))
        }.onFailure { e ->
            pendingRequests.remove(id)
            throw e
        }

        return try {
            deferred.await()
        } finally {
            pendingRequests.remove(id)
        }
    }

    private suspend fun handleIncomingJson(jsonObj: JsonObject) {
        if (jsonObj.containsKey("id") && jsonObj["id"] !is JsonNull) {
            handleResponse(jsonObj)
        } else {
            handleNotification(jsonObj)
        }
    }

    private suspend fun handleResponse(jsonObj: JsonObject) {
        val response = json.decodeFromJsonElement(
            MsmpResponse.serializer(),
            jsonObj
        )

        val deferred = pendingRequests.get(response.id) ?: return

        val rawError = response.error
        if (rawError != null) {
            val rpcError = JsonRpcError.fromCode(rawError.code, rawError.message)
            deferred.completeExceptionally(JsonRpcException(rpcError, rawError.data))
        } else {
            deferred.complete(response.result ?: JsonNull)
        }
    }

    private suspend fun handleNotification(jsonObj: JsonObject) {
        val method = jsonObj["method"]?.jsonPrimitive?.content ?: return
        val rawParams = jsonObj["params"]?.jsonArray?.firstOrNull() ?: JsonNull

        val event = when (val provider = eventMap.get(method)) {
            is MsmpEventProvider.Data<*> -> if (rawParams !is JsonNull) {
                json.decodeFromJsonElement(provider.serializer, JsonObject(mapOf("eventCtx" to rawParams)))
            } else {
                UnknownMsmpEvent(method, rawParams)
            }

            is MsmpEventProvider.Singleton -> provider.instance
            is MsmpEventProvider.Custom -> provider.block(rawParams)
            null -> UnknownMsmpEvent(method, rawParams)
        }
        eventFlow.emit(event)
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

            requestChannel.close()

            scope.launch { connection.close() }
            cleanup(finalReason)
        }
    }

    private suspend fun cleanup(reason: String) {
        val exception = IllegalStateException("Connection lost: $reason")
        pendingRequests.clearAndForEach { _, deferred ->
            deferred.completeExceptionally(exception)
        }
        scope.cancel()
    }
}
