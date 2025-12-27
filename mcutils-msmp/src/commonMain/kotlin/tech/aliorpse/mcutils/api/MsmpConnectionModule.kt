package tech.aliorpse.mcutils.api

import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.api.extension.GamerulesExtension
import tech.aliorpse.mcutils.api.extension.PlayersExtension
import tech.aliorpse.mcutils.api.extension.ServerExtension
import tech.aliorpse.mcutils.api.extension.ServerSettingsExtension
import tech.aliorpse.mcutils.api.extension.UniversalArrayExtension
import tech.aliorpse.mcutils.entity.ConnectionClosedEvent
import tech.aliorpse.mcutils.entity.MsmpEvent
import tech.aliorpse.mcutils.entity.OperatorDto
import tech.aliorpse.mcutils.entity.PlayerDto
import tech.aliorpse.mcutils.entity.UserBanDto
import tech.aliorpse.mcutils.internal.impl.MsmpConnectionImpl
import tech.aliorpse.mcutils.internal.util.WebSocketClientProvider.webSocketClient
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalMCUtilsApi
public suspend fun MCServer.createMsmpConnection(
    target: String,
    token: String,
    timeout: Long = 3000L,
    eventBufferSize: Int = 64
): MsmpConnection {
    val session = webSocketClient.webSocketSession(target) {
        header(HttpHeaders.Authorization, "Bearer $token")
        timeout { connectTimeoutMillis = timeout }
    }
    return MsmpConnection(MsmpConnectionImpl(session, eventBufferSize))
}

public class MsmpConnection internal constructor(
    @PublishedApi internal val impl: MsmpConnectionImpl
) : AutoCloseable {
    @PublishedApi
    internal val defaultScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Listen for impl closing in any reason
    init {
        defaultScope.launch {
            impl.listeningJob.join()
            if (isActive) this@MsmpConnection.close()
        }
    }

    // --- Request Basic API ---

    /**
     * The basic call method for making requests.
     *
     * This is the none-params variant.
     */
    public suspend inline fun call(
        method: String,
        timeout: Long = 10000L
    ): JsonElement = impl.call<JsonElement>(method, null, timeout)

    /**
     * The basic call method for making requests.
     *
     * This is the manually built params variant.
     * You can use this for APIs that cannot parse type information (e.g., [UniversalArrayExtension]).
     */
    public suspend inline fun call(
        method: String,
        params: JsonElement,
        timeout: Long = 10000L
    ): JsonElement = impl.call<JsonElement>(method, params, timeout)

    /**
     * The basic call method for making requests.
     *
     * This is the auto-encoded params variant.
     */
    public suspend inline fun <reified T> call(
        method: String,
        params: T,
        timeout: Long = 10000L,
    ): JsonElement = impl.call(method, params, timeout)

    /**
     * Discover the server's capabilities and features.
     */
    public suspend inline fun discover(): JsonElement = call("rpc.discover")

    // --- Request Extension API ---

    public val allowList: UniversalArrayExtension<PlayerDto>
        by lazy { UniversalArrayExtension(this, "minecraft:allowlist") }

    public val banList: UniversalArrayExtension<UserBanDto>
        by lazy { UniversalArrayExtension(this, "minecraft:bans") }

    public val operatorList: UniversalArrayExtension<OperatorDto>
        by lazy { UniversalArrayExtension(this, "minecraft:operators") }

    public val players: PlayersExtension
        by lazy { PlayersExtension(this) }

    public val server: ServerExtension
        by lazy { ServerExtension(this) }

    public val gamerules: GamerulesExtension
        by lazy { GamerulesExtension(this) }

    public val serverSettings: ServerSettingsExtension
        by lazy { ServerSettingsExtension(this) }

    // --- Event API ---

    public val eventFlow: SharedFlow<MsmpEvent> = impl.eventFlow

    public inline fun <reified T : MsmpEvent> on(): Flow<T> = impl.eventFlow.filterIsInstance<T>()

    public inline fun <reified T : MsmpEvent> on(
        scope: CoroutineScope = defaultScope,
        context: CoroutineContext = EmptyCoroutineContext,
        crossinline block: T.() -> Unit
    ): Job = scope.launch(context) { on<T>().collect { event -> block(event) } }

    // --- Universal API ---

    public suspend inline fun await(): ConnectionClosedEvent =
        eventFlow.filterIsInstance<ConnectionClosedEvent>().first()

    @OptIn(DelicateCoroutinesApi::class)
    public override fun close() {
        if (defaultScope.isActive) defaultScope.cancel()
        GlobalScope.launch { runCatching { impl.close() }.also { impl.scope.cancel() } }
    }
}
