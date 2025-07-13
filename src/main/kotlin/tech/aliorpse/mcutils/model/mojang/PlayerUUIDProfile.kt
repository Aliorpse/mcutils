package tech.aliorpse.mcutils.model.mojang

data class PlayerUUIDProfile(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val demo: Boolean = false
)
