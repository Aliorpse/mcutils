package tech.aliorpse.mcutils.model.player.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class PlayerWithUuid(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val demo: Boolean = false
)
