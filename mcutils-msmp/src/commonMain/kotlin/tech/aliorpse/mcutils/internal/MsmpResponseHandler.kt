package tech.aliorpse.mcutils.internal

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.entity.JsonRpcError
import tech.aliorpse.mcutils.entity.JsonRpcException
import tech.aliorpse.mcutils.entity.MsmpEvent
import tech.aliorpse.mcutils.entity.MsmpEventProvider
import tech.aliorpse.mcutils.entity.MsmpRequest
import tech.aliorpse.mcutils.entity.MsmpResponse
import tech.aliorpse.mcutils.entity.UnknownMsmpEvent
import tech.aliorpse.mcutils.entity.eventMap
import tech.aliorpse.mcutils.entity.fromCode
import tech.aliorpse.mcutils.internal.util.SpinLockedMutableMap
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

@OptIn(ExperimentalAtomicApi::class)
internal class MsmpResponseHandler(
    private val json: Json,
    eventBufferSize: Int
) {
    val eventFlow = MutableSharedFlow<MsmpEvent>(
        extraBufferCapacity = eventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val pendingRequests = SpinLockedMutableMap<Int, CompletableDeferred<JsonElement>>()

    private val idCounter = AtomicInt(0)

    fun registerRequest(
        method: String,
        params: JsonElement
    ): Pair<MsmpRequest, CompletableDeferred<JsonElement>> {
        val id = idCounter.fetchAndIncrement()
        val request = MsmpRequest(id = id, method = method, params = params)
        val deferred = CompletableDeferred<JsonElement>()

        pendingRequests.put(id, deferred)

        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                pendingRequests.remove(id)
            }
        }

        return request to deferred
    }

    fun cancelRequest(id: Int, cause: Throwable) {
        pendingRequests.remove(id)?.completeExceptionally(cause)
    }

    suspend fun handleIncomingElement(element: JsonElement) {
        when (element) {
            is JsonArray -> element.forEach { item ->
                if (item is JsonObject) processJsonPacket(item)
            }
            is JsonObject -> processJsonPacket(element)
            else -> error("Unexpected response element: $element")
        }
    }

    private suspend fun processJsonPacket(jsonObj: JsonObject) {
        if (jsonObj.containsKey("id") && jsonObj["id"] !is JsonNull) {
            handleResponse(jsonObj)
        } else {
            handleNotification(jsonObj)
        }
    }

    private fun handleResponse(jsonObj: JsonObject) {
        val response = json.decodeFromJsonElement(MsmpResponse.serializer(), jsonObj)
        val deferred = pendingRequests.remove(response.id) ?: return

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

    fun cleanupPendingRequests(reason: String) {
        val exception = IllegalStateException("Connection lost: $reason")
        pendingRequests.clearAndForEach { _, deferred ->
            deferred.completeExceptionally(exception)
        }
    }
}
