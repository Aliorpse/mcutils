package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable
import tech.aliorpse.mcutils.api.MinecraftServer.getStatus
import tech.aliorpse.mcutils.internal.serializer.TextComponentSerializer

/**
 * Minecraft server status by [getStatus]
 *
 * @property secureChatEnforced Whether secure chat is required.
 * @property favicon Base64-encoded favicon, if present.
 * @property ping Ping in milliseconds, null if unsupported.
 * @property srvRecord  e.g. int-p-as-1.wdsj.net:55581
 */
public data class ServerStatus(
    val description: TextComponent,
    val players: Players,
    val version: Version,
    val ping: Long,
    val secureChatEnforced: Boolean,
    val favicon: String?,
    val srvRecord: String?
)

/**
 * Player info.
 *
 * @property sample Optional sample list of online players.
 */
@Serializable
public data class Players(
    val max: Int,
    val online: Int,
    val sample: List<Sample>? = emptyList()
)

/**
 * Version info.
 *
 * @property name Version name.
 * @property protocol Protocol number.
 */
@Serializable
public data class Version(
    val name: String,
    val protocol: Long
)

/**
 * Sample player entry.
 */
@Serializable
public data class Sample(
    val id: String,
    val name: String
)

/**
 * MOTD text component.
 *
 * @property text Raw text.
 * @property color Hex color code.
 * @property styles Text styles.
 * @property extra Child components.
 */
@Serializable(with = TextComponentSerializer::class)
public data class TextComponent(
    val text: String,
    val color: String = "",
    val styles: Set<TextStyle> = emptySet(),
    val extra: List<TextComponent> = emptyList(),
)

/**
 * Text styles for MOTD.
 */
@Serializable
public enum class TextStyle {
    BOLD,
    ITALIC,
    UNDERLINED,
    STRIKETHROUGH,
    OBFUSCATED,
}
