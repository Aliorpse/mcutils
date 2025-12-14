package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable
import tech.aliorpse.mcutils.internal.serializer.TextComponentSerializer

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
