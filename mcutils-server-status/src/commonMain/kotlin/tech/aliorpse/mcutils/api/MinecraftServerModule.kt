package tech.aliorpse.mcutils.api

import tech.aliorpse.mcutils.entity.QueryStatusBasic
import tech.aliorpse.mcutils.entity.QueryStatusFull
import tech.aliorpse.mcutils.entity.ServerStatus
import tech.aliorpse.mcutils.entity.SrvRecord
import tech.aliorpse.mcutils.internal.impl.QueryImpl
import tech.aliorpse.mcutils.internal.impl.ServerListPingImpl
import tech.aliorpse.mcutils.internal.util.Punycode
import tech.aliorpse.mcutils.internal.util.resolveSrvImpl

/**
 * Fetch server status by [Server List Ping](https://minecraft.wiki/w/Java_Edition_protocol/Server_List_Ping).
 *
 * @param enableSrv Whether to use the default SRV query implementation. The default DNS server is 8.8.8.8.
 * Use [resolveSrv] for further customization. (remember to set `enableSrv = false` there)
 */
public suspend fun MinecraftServer.getStatus(
    host: String,
    port: Int = 25565,
    timeout: Long = 3000L,
    enableSrv: Boolean = true,
): ServerStatus = ServerListPingImpl.getStatus(host, port, timeout, enableSrv)

/**
 * Fetch server status by [Query](https://minecraft.wiki/w/Query).
 */
public suspend fun MinecraftServer.getQueryBasic(
    host: String,
    port: Int = 25565,
    timeout: Long = 3000L,
): QueryStatusBasic = QueryImpl.getQuery(host, port, timeout, false) as QueryStatusBasic

/**
 * Fetch server status by [Query](https://minecraft.wiki/w/Query).
 */
public suspend fun MinecraftServer.getQueryFull(
    host: String,
    port: Int = 25565,
    timeout: Long = 3000L,
): QueryStatusFull = QueryImpl.getQuery(host, port, timeout, true) as QueryStatusFull

/**
 * Resolves the SRV record for Minecraft server by the given host.
 *
 * You may need to get the system DNS server here, for Google's DNS server doesn't work in some regions.
 */
public suspend fun MinecraftServer.resolveSrv(
    host: String,
    dnsServer: String = "8.8.8.8",
    dnsPort: Int = 53,
    timeout: Long = 3000L,
): SrvRecord? = resolveSrvImpl(
    "_minecraft._tcp.${Punycode.from(host)}",
    dnsServer,
    dnsPort,
    timeout
)
