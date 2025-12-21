package tech.aliorpse.mcutils.api

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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
import tech.aliorpse.mcutils.internal.util.WebSocketClientProvider.webSocketClient
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

public suspend fun MCServer.createMsmpConnection(
    target: String,
    token: String,
    pingIntervalMillis: Long = 10_000,
    timeoutMillis: Long = 3000,
): MsmpConnection {
    val session = webSocketClient.webSocketSession(target) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }
    session.pingIntervalMillis = pingIntervalMillis
    session.timeoutMillis = timeoutMillis
    return MsmpConnection(CoroutineScope(DispatchersIO), session)
}

// I can just do like this because reified functions are prohibited from accessing internal functions.
@OptIn(ExperimentalAtomicApi::class)
public class MsmpConnection internal constructor(
    @PublishedApi internal val scope: CoroutineScope,
    @PublishedApi internal val connection: DefaultClientWebSocketSession
) {
    public val eventFlow: SharedFlow<MsmpEvent>
        field = MutableSharedFlow<MsmpEvent>()

    @PublishedApi
    internal var pendingRequests: PersistentMap<Int, CompletableDeferred<JsonElement>> = persistentMapOf()

    @PublishedApi
    internal val idCounter: AtomicInt = AtomicInt(0)

    @PublishedApi
    internal val json: Json = Json { ignoreUnknownKeys = true }

    public suspend inline fun <reified T> call(method: String, params: T?): JsonElement {
        val id = idCounter.fetchAndIncrement()
        val paramElement = json.encodeToJsonElement(params)

        val request = MsmpRequest(
            id = id,
            method = method,
            params = paramElement
        )

        val deferred = CompletableDeferred<JsonElement>()
        pendingRequests = pendingRequests.put(id, deferred)

        try {
            connection.send(json.encodeToString(MsmpRequest.serializer(JsonElement.serializer()), request))
            return deferred.await()
        } finally {
            pendingRequests = pendingRequests.remove(id)
        }
    }

    public inline fun <reified T> on(crossinline block: T.() -> Unit) where T : MsmpEvent {
        scope.launch {
            eventFlow.filterIsInstance<T>()
                .collect { event ->
                    block(event)
                }
        }
    }

    public suspend fun close(): Unit = connection.close()

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
                pendingRequests.forEach { (_, deferred) ->
                    deferred.completeExceptionally(IllegalStateException("Connection lost"))
                }
            }
        }
    }

    private fun handleResponse(jsonObj: JsonObject) {
        val response = json.decodeFromJsonElement(
            MsmpResponse.serializer(JsonElement.serializer()),
            jsonObj
        )

        val deferred = pendingRequests[response.id] ?: error("Cannot find pending request with id ${response.id}")

        if (response.error != null) {
            deferred.completeExceptionally(
                IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            )
        } else {
            deferred.complete(response.result ?: JsonNull)
        }
    }

    private suspend fun handleNotification(jsonObj: JsonObject) {
        val method = jsonObj["method"]?.jsonPrimitive?.content?.removePrefix("minecraft:notification/") ?: return
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
