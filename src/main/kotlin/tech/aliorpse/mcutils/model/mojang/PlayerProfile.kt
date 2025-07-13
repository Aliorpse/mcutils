package tech.aliorpse.mcutils.model.mojang

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okio.ByteString.Companion.decodeBase64
import java.lang.reflect.Type

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

private data class RawPlayerProfile(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val properties: List<Property>
) {
    data class Property(
        val name: String,
        val value: String
    )
}

/**
 * A custom deserializer for converting JSON data into a `PlayerProfile` object.
 *
 * This class implements `JsonDeserializer<PlayerProfile>` and provides functionality
 * to parse player profile data, including details such as UUID, name, skin URL, cape URL,
 * and skin model type, from the Mojang session server JSON response.
 *
 * The deserialization process includes:
 * - Parsing the raw profile data (`RawPlayerProfile`) to extract basic player information.
 * - Decoding and parsing Base64-encoded textures from the profile's property list.
 * - Extracting texture URLs for skin and cape as well as determining the player's skin model.
 *
 * In case of errors during the decoding or parsing of textures, default values are used.
 */
class PlayerProfileDeserializer : JsonDeserializer<PlayerProfile> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PlayerProfile {
        val rawProfile = Gson().fromJson(json, RawPlayerProfile::class.java)
        val texturesBase64 = rawProfile.properties.find { it.name == "textures" }?.value

        var skinUrl: String? = null
        var capeUrl: String? = null
        var model = SkinModel.CLASSIC

        if (texturesBase64 != null) {
            try {
                val jsonStr = String(texturesBase64.decodeBase64()!!.toByteArray())
                val texturesObj = Gson().fromJson(jsonStr, DecodedTextures::class.java)
                val skin = texturesObj.textures["SKIN"]
                val cape = texturesObj.textures["CAPE"]
                skinUrl = skin?.url
                capeUrl = cape?.url
                model = SkinModel.from(skin?.metadata?.model)
            } catch (_: Exception) {
            }
        }

        return PlayerProfile(
            id = rawProfile.id,
            name = rawProfile.name,
            legacy = rawProfile.legacy,
            skinUrl = skinUrl,
            capeUrl = capeUrl,
            skinModel = model
        )
    }

    private data class DecodedTextures(
        val timestamp: Long,
        val profileId: String,
        val profileName: String,
        val textures: Map<String, Texture>
    )

    private data class Texture(
        val url: String,
        val metadata: Metadata?
    )

    private data class Metadata(val model: String?)
}
