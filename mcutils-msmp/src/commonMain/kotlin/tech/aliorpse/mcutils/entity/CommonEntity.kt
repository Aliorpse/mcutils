package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int

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
public data class IPBanDto(
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
    val players: Set<PlayerDto>? = emptySet(),
    val started: Boolean,
    val version: ServerVersionDto
)

/**
 * @property type "boolean" or "integer", refers to the actual type of [value]
 */
@Serializable
public data class TypedGameruleDto(
    val key: String,
    val type: String,
    val value: JsonPrimitive
) {
    init {
        require(type == "boolean" || type == "integer") {
            "'type' must be either 'boolean' or 'integer'"
        }
    }

    public val valueAsInt: Int
        get() = value.int

    public val valueAsBoolean: Boolean
        get() = value.boolean
}

@Serializable
public data class KickPlayerDto(
    val player: PlayerDto,
    val message: MessageDto = MessageDto(literal = "")
)

/**
 * Should be either literal or translatable.
 */
@Serializable
public data class MessageDto(
    val translatable: String? = null,
    val translatableParams: List<String>? = null,
    val literal: String? = null
) {
    init {
        require((translatable == null) != (literal == null)) {
            "Exactly one of 'translatable' or 'literal' must be provided"
        }
    }
}

/**
 * @property overlay Whether the message should be displayed in the actionbar.
 */
@Serializable
public data class SystemMessageDto(
    val receivingPlayers: Set<PlayerDto>,
    val message: MessageDto,
    val overlay: Boolean = false
)

@Serializable
public data class UntypedGameruleDto(
    val key: String,
    val value: JsonElement
)
