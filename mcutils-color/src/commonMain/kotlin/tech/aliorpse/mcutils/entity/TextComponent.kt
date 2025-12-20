package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable

/**
 * Colored minecraft text representation.
 *
 * @property text Raw text.
 * @property color Hex color code.
 * @property styles Text styles.
 * @property extra Child components.
 */
public data class TextComponent(
    val text: String,
    val color: String = "",
    val styles: Set<TextStyle> = emptySet(),
    val extra: List<TextComponent> = emptyList(),
) {
    public companion object
}

/**
 * Text styles for [TextComponent].
 */
@Serializable
public enum class TextStyle {
    BOLD,
    ITALIC,
    UNDERLINED,
    STRIKETHROUGH,
    OBFUSCATED,
}
