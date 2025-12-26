package tech.aliorpse.mcutils.api

import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.websocket.close
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.api.api.GamerulesApi
import tech.aliorpse.mcutils.api.api.PlayersApi
import tech.aliorpse.mcutils.api.api.ServerApi
import tech.aliorpse.mcutils.api.api.ServerSettingsApi
import tech.aliorpse.mcutils.api.api.UniversalArrayApi
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
    pingIntervalMillis: Long = 10000L,
    timeoutMillis: Long = 3000L,
    eventBufferSize: Int = 64
): MsmpConnection {
    val session = webSocketClient.webSocketSession(target) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }
    session.pingIntervalMillis = pingIntervalMillis
    session.timeoutMillis = timeoutMillis
    return MsmpConnection(MsmpConnectionImpl(session, eventBufferSize))
}

public class MsmpConnection internal constructor(
    @PublishedApi internal val impl: MsmpConnectionImpl
) : AutoCloseable {
    @PublishedApi
    internal val defaultScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // --- Request Basic API ---

    public suspend inline fun call(
        method: String,
        timeout: Long = 10000L
    ): JsonElement = impl.call<JsonElement>(method, null, timeout)

    public suspend inline fun <reified T> call(
        method: String,
        params: T,
        timeout: Long = 10000L,
    ): JsonElement = impl.call(method, params, timeout)

    public suspend inline fun discover(): JsonElement = call("rpc.discover")

    // --- Request Extension API ---

    public val allowList: UniversalArrayApi<PlayerDto>
        by lazy { UniversalArrayApi(this, "minecraft:allowlist", PlayerDto.serializer()) }

    public val banList: UniversalArrayApi<UserBanDto>
        by lazy { UniversalArrayApi(this, "minecraft:bans", UserBanDto.serializer()) }

    public val operatorList: UniversalArrayApi<OperatorDto>
        by lazy { UniversalArrayApi(this, "minecraft:operators", OperatorDto.serializer()) }

    public val players: PlayersApi
        by lazy { PlayersApi(this) }

    public val server: ServerApi
        by lazy { ServerApi(this) }

    public val gamerules: GamerulesApi
        by lazy { GamerulesApi(this) }

    public val serverSettings: ServerSettingsApi
        by lazy { ServerSettingsApi(this) }

    // --- Event API ---

    public val eventFlow: SharedFlow<MsmpEvent> = impl.eventFlow

    public inline fun <reified T : MsmpEvent> on(): Flow<T> = impl.eventFlow.filterIsInstance<T>()

    public inline fun <reified T : MsmpEvent> on(
        scope: CoroutineScope = defaultScope,
        context: CoroutineContext = EmptyCoroutineContext,
        crossinline block: T.() -> Unit
    ): Job = scope.launch(context) { on<T>().collect { event -> block(event) } }

    // --- Universal API ---

    @OptIn(DelicateCoroutinesApi::class)
    public override fun close() {
        if (defaultScope.isActive) defaultScope.cancel()
        if (impl.scope.isActive) impl.scope.cancel()
        GlobalScope.launch { impl.connection.close() }
    }
}
