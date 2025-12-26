package tech.aliorpse.mcutils.internal.impl

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.entity.EventProvider
import tech.aliorpse.mcutils.entity.MsmpEvent
import tech.aliorpse.mcutils.entity.MsmpRequest
import tech.aliorpse.mcutils.entity.MsmpResponse
import tech.aliorpse.mcutils.entity.UnknownMsmpEvent
import tech.aliorpse.mcutils.entity.eventMap
import tech.aliorpse.mcutils.internal.util.DispatchersIO
import tech.aliorpse.mcutils.internal.util.MutexMutableMap
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

@PublishedApi
@OptIn(ExperimentalAtomicApi::class)
internal class MsmpConnectionImpl internal constructor(
    @PublishedApi internal val connection: DefaultClientWebSocketSession,
    eventBufferSize: Int
) {
    @PublishedApi
    internal val scope = CoroutineScope(DispatchersIO + SupervisorJob())

    @PublishedApi
    internal val eventFlow = MutableSharedFlow<MsmpEvent>(
        extraBufferCapacity = eventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @PublishedApi
    internal val pendingRequests = MutexMutableMap<Int, CompletableDeferred<JsonElement>>()

    @PublishedApi
    internal val idCounter = AtomicInt(0)

    @PublishedApi
    internal val json = Json { ignoreUnknownKeys = true }

    @PublishedApi
    internal suspend inline fun <reified T> call(method: String, params: T?, timeout: Long): JsonElement {
        val id = idCounter.fetchAndIncrement()
        val paramElement = if (params != null) json.encodeToJsonElement(listOf(params)) else JsonArray(emptyList())

        val request = MsmpRequest(
            id = id,
            method = method,
            params = paramElement
        )

        val deferred = CompletableDeferred<JsonElement>()
        pendingRequests.put(id, deferred)

        return try {
            withTimeout(timeout) {
                connection.send(json.encodeToString(MsmpRequest.serializer(), request))
                deferred.await()
            }
        } finally {
            pendingRequests.remove(id)
        }
    }

    init {
        scope.launch {
            try {
                connection.incoming.receiveAsFlow()
                    .filterIsInstance<Frame.Text>()
                    .map { it.readText() }
                    .collect { text ->
                        val jsonObj = json.parseToJsonElement(text).jsonObject
                        if (jsonObj.containsKey("id") && jsonObj["id"] !is JsonNull) {
                            handleResponse(jsonObj)
                        } else {
                            handleNotification(jsonObj)
                        }
                    }
            } finally {
                pendingRequests.forEach { _, deferred ->
                    deferred.completeExceptionally(IllegalStateException("Connection lost"))
                }
            }
        }
    }

    private suspend fun handleResponse(jsonObj: JsonObject) {
        val response = json.decodeFromJsonElement(
            MsmpResponse.serializer(),
            jsonObj
        )

        val deferred = pendingRequests.get(response.id) ?: return

        if (response.error != null) {
            deferred.completeExceptionally(
                IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            )
        } else {
            deferred.complete(response.result ?: JsonNull)
        }
    }

    private suspend fun handleNotification(jsonObj: JsonObject) {
        val method = jsonObj["method"]?.jsonPrimitive?.content ?: return
        val rawParams = jsonObj["params"]?.jsonArray?.firstOrNull() ?: JsonNull

        val wrappedParams = JsonObject(mapOf("eventCtx" to rawParams))

        val event = when (val provider = eventMap[method]) {
            is EventProvider.Data<*> -> {
                if (rawParams !is JsonNull) {
                    json.decodeFromJsonElement(provider.serializer, wrappedParams)
                } else {
                    UnknownMsmpEvent(method, rawParams)
                }
            }
            is EventProvider.Singleton -> provider.instance
            is EventProvider.Custom -> provider.block(rawParams)
            null -> UnknownMsmpEvent(method, rawParams)
        }

        eventFlow.emit(event)
    }
}
