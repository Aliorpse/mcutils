package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable
import tech.aliorpse.mcutils.internal.serializer.TextComponentSerializer

/**
 * Server List Ping status.
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

public interface QueryStatus {
    public val description: String
    public val map: String
    public val players: Players
}

/**
 * Query basic status.
 *
 * @property map Name of the current map.
 */
public data class QueryStatusBasic(
    override val description: String,
    override val map: String,
    override val players: Players,
) : QueryStatus

public data class QueryStatusFull(
    override val description: String,
    override val map: String,
    override val players: Players,
    val version: String,
    val plugins: Set<String>,
) : QueryStatus

/**
 * Player info.
 *
 * @property sample Optional sample list of online players.
 */
@Serializable
public data class Players(
    val max: Int,
    val online: Int,
    val sample: Set<Sample>? = emptySet()
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
