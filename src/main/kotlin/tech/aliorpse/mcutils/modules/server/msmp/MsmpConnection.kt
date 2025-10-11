package tech.aliorpse.mcutils.modules.server.msmp

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.annotations.InternalMcUtilsApi
import tech.aliorpse.mcutils.exceptions.MsmpResponseException
import tech.aliorpse.mcutils.model.server.msmp.jsonrpc.JsonRpcError
import tech.aliorpse.mcutils.model.server.msmp.jsonrpc.JsonRpcRequest
import tech.aliorpse.mcutils.modules.server.msmp.api.AllowlistModule
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException

public class MsmpConnection internal constructor(
    internal val session: DefaultClientWebSocketSession,
    internal val token: String
) {
    public val allowlist: AllowlistModule = AllowlistModule(this)

    private val json = Json { ignoreUnknownKeys = true }
    private val pending = ConcurrentHashMap<Int, CompletableDeferred<JsonElement>>()
    private val idCounter = AtomicInteger(0)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            for (frame in session.incoming) {
                val text = (frame as Frame.Text).readText()
                val root = runCatching { json.parseToJsonElement(text) }.getOrNull() ?: continue
                val id = root.jsonObject["id"]?.jsonPrimitive?.intOrNull ?: continue

                val deferred = pending.remove(id)
                deferred?.complete(root)
            }
        }
    }

    /**
     * Calls a JSON-RPC method and returns the result as a list.
     *
     * - Automatically adds the `minecraft:` prefix for [method] if missing.
     * - Builds the `params` field according to the argument type:
     *   - `null` → `[]`
     *   - `List<T>` → `[[...]]`
     *   - Single object `T` → `[ {...} ]`
     */
    @InternalMcUtilsApi
    public suspend fun <T> call(
        serializer: KSerializer<T>,
        method: String,
        params: Any? = null,
    ): List<T> {
        val method = if (method.startsWith("minecraft:")) method else "minecraft:$method"

        @Suppress("UNCHECKED_CAST")
        val payloadJson = when (params) {
            null -> JsonArray(emptyList())
            is List<*> -> {
                val list = params as List<T>
                json.encodeToJsonElement(
                    ListSerializer(ListSerializer(serializer)),
                    listOf(list)
                )
            }
            else -> {
                val single = params as T
                json.encodeToJsonElement(
                    ListSerializer(serializer),
                    listOf(single)
                )
            }
        }

        val id = idCounter.incrementAndGet()
        val deferred = CompletableDeferred<JsonElement>()

        pending[id] = deferred
        session.send(
            Frame.Text(
                json.encodeToString(
                    JsonRpcRequest.serializer(), JsonRpcRequest(
                        id = id,
                        method = method,
                        params = payloadJson
                    )
                )
            )
        )

        val raw = deferred.await()
        val root = raw.jsonObject

        return when {
            "result" in root -> json.decodeFromJsonElement(ListSerializer(serializer), root["result"]!!)
            "error" in root -> {
                val error = json.decodeFromJsonElement(JsonRpcError.serializer(), root["error"]!!)
                throw MsmpResponseException(error, root["id"]?.jsonPrimitive?.intOrNull)
            }
            else -> error("Invalid JSON-RPC response: no result or error field")
        }
    }

    /**
     * Close the connection. Will throw [CancellationException] for every pending requests.
     */
    @JvmAsync
    @JvmBlocking
    public suspend fun close() {
        scope.cancel()
        pending.values.forEach {
            it.completeExceptionally(CancellationException("MsmpConnection closed"))
        }
        pending.clear()
        session.close()
    }
}
