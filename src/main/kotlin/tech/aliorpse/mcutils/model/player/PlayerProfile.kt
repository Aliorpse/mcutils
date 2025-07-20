package tech.aliorpse.mcutils.model.player

import com.squareup.moshi.JsonClass

/**
 * Represents a player's profile as retrieved from the Mojang session servers.
 *
 * @property id The UUID of the player.
 * @property name The username of the player.
 * @property legacy Uh, I don't want to explain this, check wiki, please.
 * @property skinUrl The URL to the player's skin texture.
 * @property capeUrl The URL to the player's cape texture.
 * @property skinModel The model type of the player's skin, either CLASSIC (Steve) or SLIM (Alex).
 */
data class PlayerProfile(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val skinUrl: String?,
    val capeUrl: String?,
    val skinModel: SkinModel
)

/**
 * Enum representing the model type of player's skin.
 *
 * The player's skin model can too be:
 * - CLASSIC: The default model type, also known as "Steve".
 * - SLIM: A more slender model type, also known as "Alex".
 */
enum class SkinModel {
    CLASSIC, SLIM;

    companion object {
        fun from(name: String?): SkinModel = when (name?.lowercase()) {
            "slim" -> SLIM
            else -> CLASSIC
        }
    }
}

/**
 * The JSON Element that to be processed.
 */
@JsonClass(generateAdapter = true)
data class RawPlayerProfile(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val properties: List<Property>
) {
    /**
     * @property name Always 'texture'.
     * @property value Base64 string containing [DecodedTextures].
     */
    @JsonClass(generateAdapter = true)
    data class Property(val name: String, val value: String)
}

/**
 * Texture element decoded from [RawPlayerProfile.Property.value].
 */
@JsonClass(generateAdapter = true)
data class DecodedTextures(
    val timestamp: Long,
    val profileId: String,
    val profileName: String,
    val textures: Map<String, Texture>
)

/**
 * Data of the texture.
 */
@JsonClass(generateAdapter = true)
data class Texture(
    val url: String,
    val metadata: Metadata?
)

/**
 * @property model SLIM or CLASSIC.
 */
@JsonClass(generateAdapter = true)
data class Metadata(val model: String?)
