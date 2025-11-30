package tech.aliorpse.mcutils.api

import io.ktor.network.sockets.aSocket
import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.entity.QueryStatusBasic
import tech.aliorpse.mcutils.entity.QueryStatusFull
import tech.aliorpse.mcutils.entity.ServerStatus
import tech.aliorpse.mcutils.internal.impl.QueryImpl
import tech.aliorpse.mcutils.internal.impl.RconConnectionImpl
import tech.aliorpse.mcutils.internal.impl.ServerListPingImpl
import tech.aliorpse.mcutils.internal.util.Punycode
import tech.aliorpse.mcutils.internal.util.SrvResolver
import tech.aliorpse.mcutils.internal.util.globalSelectorIO
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

public typealias MCServer = MinecraftServer

public object MinecraftServer {
    private val statusImpl by lazy { ServerListPingImpl() }
    private val queryImpl by lazy { QueryImpl() }

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
    ): ServerStatus = statusImpl.getStatus(host, port, timeout, enableSrv)

    /**
     * Create a RCON connection to a Minecraft server.
     *
     * @param password RCON password for authentication.
     */
    @JsPromise
    @JvmAsync
    @JvmBlocking
    @JvmStatic
    @JvmOverloads
    @ExperimentalMCUtilsApi
    public suspend fun createRconConnection(
        host: String,
        port: Int = 25575,
        password: String,
        timeout: Long = 10000L,
    ): RconConnection {
        val impl = RconConnectionImpl(
            aSocket(globalSelectorIO).tcp()
                .connect(Punycode.from(host), port) { socketTimeout = timeout }
        )
        impl.authenticate(password)
        return RconConnection(impl)
    }

    /**
     * Fetch server status by [Query](https://minecraft.wiki/w/Query)
     */
    @JsPromise
    @JvmAsync
    @JvmBlocking
    @JvmStatic
    @JvmOverloads
    @ExperimentalMCUtilsApi
    public suspend fun getQueryBasic(
        host: String,
        port: Int = 25565,
        timeout: Long = 3000L,
    ): QueryStatusBasic = queryImpl.getQuery(host, port, timeout, false) as QueryStatusBasic

    @JsPromise
    @JvmAsync
    @JvmBlocking
    @JvmStatic
    @JvmOverloads
    @ExperimentalMCUtilsApi
    public suspend fun getQueryFull(
        host: String,
        port: Int = 25565,
        timeout: Long = 3000L,
    ): QueryStatusFull = queryImpl.getQuery(host, port, timeout, true) as QueryStatusFull
}

public class RconConnection internal constructor(
    private val impl: RconConnectionImpl
) : AutoCloseable {
    /**
     * Execute the given command.
     *
     * - Commands run sequentially; concurrent calls are queued until the previous one finishes.
     * - The command **must not** start with a leading "/".
     * - The command size must be **less than** 1447 bytes.
     * - On server-side failure, the response will be:
     *   "Error executing: $command ($message)".
     */
    @JsPromise
    @JvmAsync
    @JvmBlocking
    @ExperimentalMCUtilsApi
    public suspend fun execute(command: String): String = impl.execute(command)

    @JsPromise
    @JvmAsync
    @JvmBlocking
    @ExperimentalMCUtilsApi
    override fun close(): Unit = impl.connection.close()
}
