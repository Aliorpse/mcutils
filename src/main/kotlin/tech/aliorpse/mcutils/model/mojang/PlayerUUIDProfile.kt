package tech.aliorpse.mcutils.model.mojang

/**
 * This data class should not be used in your app. Only for internal use.
 */
data class PlayerUUIDProfile(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val demo: Boolean = false
)
