package tech.aliorpse.mcutils.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

sealed class ServerStatus {
    abstract val description: Description?
    abstract val players: Players
    abstract val version: Version
    abstract val ping: Long
}

data class JavaServerStatus(
    override val description: Description?,
    override val players: Players,
    override val version: Version,
    override val ping: Long,
    val enforcesSecureChat: Boolean = false,
    val favicon: String?
) : ServerStatus()

data class BedrockServerStatus(
    override val description: Description?,
    override val players: Players,
    override val version: Version,
    override val ping: Long,
    val levelName: String,
    val gameMode: GameMode,
    val serverUniqueID: String,
) : ServerStatus()

/**
 * Player information structure.
 *
 * @property sample A sample list of online players (not work on bedrock servers).
 */
data class Players(
    val max: Int,
    val online: Int,
    val sample: List<Sample>
)

/**
 * Server version information.
 *
 * @property name The version name.
 * @property protocol The protocol version number.
 */
data class Version(
    val name: String,
    val protocol: Int
)

/**
 * Server description structure, typically the MOTD text.
 */
data class Description(
    val text: String
)

/**
 * Sample player information representing some online players.
 */
data class Sample(
    val id: String,
    val name: String
)

enum class GameMode {
    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SPECTATOR;
}

/**
 * Custom deserializer for Java server to handle the possible formats of the Description field.
 *
 * The Description field can be in one of two formats:
 * 1. A simple string, e.g., "A Minecraft Server"
 * 2. An object, e.g., {"text":"A Minecraft Server"}
 *
 * This deserializer processes both formats to produce a Description instance.
 */
class JavaServerDescriptionDeserializer : JsonDeserializer<Description> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Description {
        return when {
            json.isJsonPrimitive -> Description(json.asString)
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val text = obj.get("text")?.asString ?: ""
                Description(text)
            }
            else -> Description("")
        }
    }
}
