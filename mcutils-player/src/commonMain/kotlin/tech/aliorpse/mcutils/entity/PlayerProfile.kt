package tech.aliorpse.mcutils.entity

/**
 * Player profile from Mojang session servers.
 *
 * @property id Player's UUID without dashes.
 * @property legacy True if this is a legacy account.
 */
public data class PlayerProfile(
    val id: String,
    val name: String,
    val legacy: Boolean,
    val skinUrl: String?,
    val capeUrl: String?,
    val skinModel: SkinModel
)

/**
 * Player skin models.
 */
public enum class SkinModel {
    /** Default model type ("Steve"). */
    CLASSIC,

    /** Slim model type ("Alex"). */
    SLIM;

    public companion object {
        /** Returns a SkinModel from a string name. Defaults to CLASSIC. */
        public fun from(name: String?): SkinModel = when (name?.lowercase()) {
            "slim" -> SLIM
            else -> CLASSIC
        }
    }
}
