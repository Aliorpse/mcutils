package tech.aliorpse.mcutils.model.server.msmp.common

import kotlinx.serialization.Serializable

/**
 * Basic player information.
 *
 * @param id UUID with dashes.
 *
 * Either `id` or `name` should be set. `id` has higher priority.
 */
@Serializable
public data class PlayerDto(
    val id: String? = null,
    val name: String? = null
)

/**
 * Player ban record.
 *
 * @param source Operator.
 */
@Serializable
public data class UserBanDto(
    val expires: String,
    val player: PlayerDto,
    val reason: String,
    val source: String
)

/**
 * IP ban record.
 *
 * @param source Operator.
 */
@Serializable
public data class IpBanDto(
    val expires: String,
    val ip: String,
    val reason: String,
    val source: String
)

/**
 * Operator (admin) entry.
 *
 * @param bypassesPlayerLimit Can join if server full.
 */
@Serializable
public data class OperatorDto(
    val bypassesPlayerLimit: Boolean,
    val permissionLevel: Int,
    val player: PlayerDto
)

/**
 * Server state overview.
 */
@Serializable
public data class ServerState(
    val player: List<PlayerDto>,
    val started: Boolean,
    val version: ServerVersion
)

/**
 * Server version info.
 */
@Serializable
public data class ServerVersion(
    val name: String,
    val protocol: Int
)

/**
 * Game rule info.
 *
 * @param key Rule ID
 * @param type "boolean" or "integer"
 */
@Serializable
public data class TypedRule(
    val key: String,
    val type: String,
    val value: String
)
