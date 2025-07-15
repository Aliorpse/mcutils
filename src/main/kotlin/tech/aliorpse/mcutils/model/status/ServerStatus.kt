package tech.aliorpse.mcutils.model.status

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import tech.aliorpse.mcutils.util.MOTDParser.objectToSectionFormat
import tech.aliorpse.mcutils.util.MOTDParser.sectionFormatToObject
import tech.aliorpse.mcutils.util.Color
import java.lang.reflect.Type
import java.util.Locale

/**
 * Represents the status of a server.
 *
 * This class serves as a sealed base for different types of server status implementations,
 * such as JavaServerStatus or BedrockServerStatus.
 *
 * @property description Description of the server, often referred to as the MOTD text.
 * @property players Information about the players currently online and the maximum capacity.
 * @property version Information about the server's version, including name and protocol.
 * @property ping The time taken to ping the server, measured in milliseconds.
 */
sealed class ServerStatus {
    abstract val description: Description
    abstract val players: Players
    abstract val version: Version
    abstract val ping: Long
}

/**
 * Represents the status of a Java Edition Minecraft server.
 *
 * This data class extends from the sealed class `ServerStatus` and provides additional
 * information specific to Java servers, including whether the server enforces secure chat and
 * an optional favicon for the server.
 *
 * @property enforcesSecureChat Indicates whether the server enforces secure chat.
 * @property favicon An optional base64-encoded string representing the server's favicon.
 */
data class JavaServerStatus(
    override val description: Description,
    override val players: Players,
    override val version: Version,
    override val ping: Long,
    val enforcesSecureChat: Boolean = false,
    val favicon: String?
) : ServerStatus()

/**
 * Represents the status of a Minecraft Bedrock server.
 *
 * This class extends the ServerStatus base class and provides additional
 * properties specific to Bedrock servers, including level name, game mode,
 * and server unique ID.
 *
 * @property levelName The name of the level currently loaded on the server.
 * @property gameMode The current game mode of the server, such as SURVIVAL or CREATIVE.
 * @property serverUniqueID The unique identifier of the server instance.
 */
data class BedrockServerStatus(
    override val description: Description,
    override val players: Players,
    override val version: Version,
    override val ping: Long,
    val levelName: String,
    val gameMode: GameMode,
    val serverUniqueID: String,
) : ServerStatus()

/**
 * Players in server information.
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
    val text: String,
    val obj: MOTDTextComponent
)

/**
 * Sample player information representing some online players.
 */
data class Sample(
    val id: String,
    val name: String
)

/**
 * Represents the various game modes. For [tech.aliorpse.mcutils.module.status.BedrockPing.getStatus].
 */
enum class GameMode {
    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SPECTATOR,
    UNKNOWN;
}

/**
 * Represents a component of the MOTD.
 *
 * @property text The textual content of this MOTD component.
 * @property color The color of the text, Defaults to [Color.Named.WHITE].
 * @property extra Rescue. Please check wiki for this.
 */
data class MOTDTextComponent(
    val text: String,

    val color: Color? = Color.Named.WHITE,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underlined: Boolean = false,
    val strikethrough: Boolean = false,
    val obfuscated: Boolean = false,

    val extra: List<MOTDTextComponent>? = emptyList(),
)

/**
 * Deserializer used for adapt different [Description] formats sent by server.
 */
class DescriptionDeserializer : JsonDeserializer<Description> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Description {
        return when {
            json.isJsonPrimitive -> {
                Description(
                    json.asString,
                    sectionFormatToObject(json.asString)
                )
            }
            json.isJsonObject -> {
                val motdComponent = context.deserialize<MOTDTextComponent>(json, MOTDTextComponent::class.java)
                Description(
                    objectToSectionFormat(motdComponent),
                    motdComponent
                )
            }
            else -> {
                Description("", MOTDTextComponent(""))
            }
        }
    }
}

/**
 * Deserializer used for adapt different color types.
 */
class ColorTypeAdapter : JsonDeserializer<Color>, JsonSerializer<Color> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Color {
        return Color.fromString(json.asString)
            ?: throw JsonParseException("Invalid color: ${json.asString}")
    }

    override fun serialize(src: Color, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(
            when (src) {
                is Color.Named -> src.name.lowercase(Locale.ROOT)
                is Color.Custom -> src.hex
            }
        )
    }
}

/**
 * Someone just didn't follow the rules sending the MOTD text. Fuck them.
 */
class MOTDTextComponentDeserializer : JsonDeserializer<MOTDTextComponent> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MOTDTextComponent {
        if (json.isJsonPrimitive) {
            return MOTDTextComponent(text = json.asString)
        }

        val obj = json.asJsonObject
        val text = obj.get("text")?.asString ?: ""
        val color = Color.fromString(obj.get("color")?.asString ?: "WHITE")
        val bold = obj.get("bold")?.asBoolean ?: false
        val italic = obj.get("italic")?.asBoolean ?: false
        val underlined = obj.get("underlined")?.asBoolean ?: false
        val strikethrough = obj.get("strikethrough")?.asBoolean ?: false
        val obfuscated = obj.get("obfuscated")?.asBoolean ?: false

        val extraList = buildList {
            val extraArray = obj.getAsJsonArray("extra")
            if (extraArray != null) {
                for (el in extraArray) {
                    add(
                        when {
                            el.isJsonPrimitive -> MOTDTextComponent(el.asString)
                            el.isJsonObject -> context.deserialize(el, MOTDTextComponent::class.java)
                            else -> MOTDTextComponent("")
                        }
                    )
                }
            }
        }

        return MOTDTextComponent(
            text = text,
            color = color,
            bold = bold,
            italic = italic,
            underlined = underlined,
            strikethrough = strikethrough,
            obfuscated = obfuscated,
            extra = extraList
        )
    }
}
