package tech.aliorpse.mcutils.api

import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.entity.QueryStatusBasic
import tech.aliorpse.mcutils.entity.QueryStatusFull
import tech.aliorpse.mcutils.entity.ServerStatus
import tech.aliorpse.mcutils.internal.impl.QueryImpl
import tech.aliorpse.mcutils.internal.impl.ServerListPingImpl

private val statusImpl by lazy { ServerListPingImpl() }
private val queryImpl by lazy { QueryImpl() }

/**
 * Fetch server status by [Server List Ping](https://minecraft.wiki/w/Java_Edition_protocol/Server_List_Ping).
 *
 * @param enableSrv Whether to use default SRV query implementation.
 */
public suspend fun MinecraftServer.getStatus(
    host: String,
    port: Int = 25565,
    timeout: Long = 3000L,
    enableSrv: Boolean = true,
): ServerStatus = statusImpl.getStatus(host, port, timeout, enableSrv)

/**
 * Fetch server status by [Query](https://minecraft.wiki/w/Query)
 */
@ExperimentalMCUtilsApi
public suspend fun MinecraftServer.getQueryBasic(
    host: String,
    port: Int = 25565,
    timeout: Long = 3000L,
): QueryStatusBasic = queryImpl.getQuery(host, port, timeout, false) as QueryStatusBasic

/**
 * Fetch server status by [Query](https://minecraft.wiki/w/Query)
 */
@ExperimentalMCUtilsApi
public suspend fun MinecraftServer.getQueryFull(
    host: String,
    port: Int = 25565,
    timeout: Long = 3000L,
): QueryStatusFull = queryImpl.getQuery(host, port, timeout, true) as QueryStatusFull
