package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
public data class PlayerDto(
    val id: String? = null,
    val name: String? = null
)

@Serializable
public data class UserBanDto(
    val expires: String? = "forever",
    val player: PlayerDto,
    val reason: String? = null,
    val source: String? = null
)

@Serializable
public data class IpBanDto(
    val expires: String? = "forever",
    val ip: String,
    val reason: String? = null,
    val source: String? = null
)

@Serializable
public data class OperatorDto(
    val bypassesPlayerLimit: Boolean,
    val permissionLevel: Int,
    val player: PlayerDto
)

@Serializable
public data class ServerVersionDto(
    val name: String,
    val protocol: Int
)

@Serializable
public data class ServerStateDto(
    val players: List<PlayerDto>,
    val started: Boolean,
    val version: ServerVersionDto
)

@Serializable
public data class TypedRuleDto(
    val key: String,
    val type: String, // "boolean" or "integer"
    val value: JsonPrimitive
)
