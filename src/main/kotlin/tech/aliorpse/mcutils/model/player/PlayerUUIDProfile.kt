package tech.aliorpse.mcutils.model.player

import com.squareup.moshi.JsonClass

/**
 * This data class should not be used in your app. Only for internal use.
 */
@JsonClass(generateAdapter = true)
internal data class PlayerUUIDProfile(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val demo: Boolean = false
)
