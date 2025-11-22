package tech.aliorpse.mcutils.api

import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.entity.ServerStatus
import tech.aliorpse.mcutils.internal.impl.ServerStatusImpl
import tech.aliorpse.mcutils.internal.util.SrvResolver
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

public typealias MCServer = MinecraftServer

public object MinecraftServer {
    /**
     * Fetch server status by [Server List Ping](https://minecraft.wiki/w/Java_Edition_protocol/Server_List_Ping).
     *
     * @param enableSrv Whether to use default SRV query implementation (Google DoH, see [SrvResolver]).
     */
    @JsPromise
    @JvmAsync
    @JvmBlocking
    @JvmStatic
    @JvmOverloads
    public suspend fun getStatus(
        host: String,
        port: Int = 25565,
        timeout: Long = 3000L,
        enableSrv: Boolean = true,
    ): ServerStatus = ServerStatusImpl.getStatus(host, port, timeout, enableSrv)
}
