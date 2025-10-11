package tech.aliorpse.mcutils.model.player

/**
 * Represents a player's profile as retrieved from the Mojang session servers.
 *
 * @property id The UUID of the player.
 * @property name The username of the player.
 * @property legacy True when it is a legacy account.
 * @property skinUrl The URL to the player's skin texture.
 * @property capeUrl The URL to the player's cape texture.
 * @property skinModel The model type of the player's skin, either CLASSIC (Steve) or SLIM (Alex).
 */
public data class PlayerProfile(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val skinUrl: String? = null,
    val capeUrl: String? = null,
    val skinModel: SkinModel
)

/**
 * Enum representing the model type of player's skin.
 *
 * The player's skin model can be:
 * - CLASSIC: The default model type, also known as "Steve".
 * - SLIM: A more slender model type, also known as "Alex".
 */
public enum class SkinModel {
    CLASSIC, SLIM;

    public companion object {
        public fun from(name: String?): SkinModel = when (name?.lowercase()) {
            "slim" -> SLIM
            else -> CLASSIC
        }
    }
}
