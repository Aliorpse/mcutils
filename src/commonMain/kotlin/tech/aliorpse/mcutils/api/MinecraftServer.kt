package tech.aliorpse.mcutils.api

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.entity.BedrockServerStatus
import tech.aliorpse.mcutils.entity.JavaServerStatus
import tech.aliorpse.mcutils.internal.impl.BedrockServerStatusImpl
import tech.aliorpse.mcutils.internal.impl.JavaServerStatusImpl
import tech.aliorpse.mcutils.internal.util.SrvResolver
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

public object MinecraftServer {
    /**
     * Fetch Java server status by [Server List Ping](https://minecraft.wiki/w/Java_Edition_protocol/Server_List_Ping).
     *
     * @param enableSrv Whether to use default SRV query implementation (Google DoH, see [SrvResolver]).
     */
    @JsPromise
    @JvmAsync
    @JvmBlocking
    @JvmStatic
    @JvmOverloads
    public suspend fun getJavaStatus(
        host: String,
        port: Int = 25565,
        timeout: Long = 3000L,
        enableSrv: Boolean = true,
    ): JavaServerStatus = JavaServerStatusImpl.getStatus(host, port, timeout, enableSrv)

    /**
     * Fetch Bedrock server status by [Unconnected Pings](https://wiki.bedrock.dev/servers/raknet.html).
     *
     * Note: Ktor UDP client does not support configurable timeouts,
     * so this function may throw [TimeoutCancellationException] if the request times out.
     */
    @JsPromise
    @JvmAsync
    @JvmBlocking
    @JvmStatic
    @JvmOverloads
    public suspend fun getBedrockStatus(
        host: String,
        port: Int = 19132,
        timeout: Long = 3000L,
    ): BedrockServerStatus = withTimeout(timeout) {
        BedrockServerStatusImpl.getStatus(host, port)
    }
}
