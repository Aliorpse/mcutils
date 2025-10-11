package tech.aliorpse.mcutils.model.server.status

import kotlinx.serialization.Serializable

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
public interface ServerStatus {
    public val description: TextComponent
    public val players: Players
    public val version: Version
    public val ping: Long?
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
 * @property ping Will be null when remote doesn't support.
 */
@Serializable(with = JavaServerStatusSerializer::class)
public data class JavaServerStatus(
    override val description: TextComponent,
    override val players: Players,
    override val version: Version,
    override val ping: Long?,
    val enforcesSecureChat: Boolean = false,
    val favicon: String?
) : ServerStatus

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
public data class BedrockServerStatus(
    override val description: TextComponent,
    override val players: Players,
    override val version: Version,
    override val ping: Long,
    val levelName: String,
    val gameMode: GameMode,
    val serverUniqueID: String,
) : ServerStatus

/**
 * Players in server information.
 *
 * @property sample A sample list of online players (not work on bedrock servers).
 */
@Serializable
public data class Players(
    val max: Int,
    val online: Int,
    val sample: List<Sample>? = emptyList()
)

/**
 * Server version information.
 *
 * @property name The version name.
 * @property protocol The protocol version number.
 */
@Serializable
public data class Version(
    val name: String,
    val protocol: Long
)

/**
 * Sample player information representing some online players.
 */
@Serializable
public data class Sample(
    val id: String,
    val name: String
)

/**
 * Represents the various game modes. For [tech.aliorpse.mcutils.modules.server.status.BedrockServer.getStatus].
 */
public enum class GameMode {
    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SPECTATOR,
    UNKNOWN
}

/**
 * Represents a component of the MOTD.
 *
 * @property text The textual content of this MOTD component.
 * @property color The color of the text. Hex codes.
 * @property extra Rescue. Please check the wiki for this.
 */
@Serializable(with = TextComponentSerializer::class)
public data class TextComponent(
    val text: String,
    val color: String = "",
    val styles: Set<TextStyle> = emptySet(),
    val extra: List<TextComponent> = emptyList(),
)

/**
 * Proper styles for the [TextComponent].
 */
@Serializable
public enum class TextStyle {
    BOLD,
    ITALIC,
    UNDERLINED,
    STRIKETHROUGH,
    OBFUSCATED,
}
