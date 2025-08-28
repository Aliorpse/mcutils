package tech.aliorpse.mcutils.utils

import tech.aliorpse.mcutils.model.server.TextComponent
import tech.aliorpse.mcutils.model.server.TextStyle
import java.util.EnumSet

/**
 * Coverts string with "§" to a [TextComponent].
 */
internal fun String.toTextComponent(): TextComponent {
    val components = mutableListOf<TextComponent>()
    var currentText = StringBuilder()
    var currentColor = ""
    val currentStyles: EnumSet<TextStyle> = EnumSet.noneOf(TextStyle::class.java)

    val iterator = this.iterator()
    while (iterator.hasNext()) {
        val c = iterator.nextChar()
        if (c == '§' && iterator.hasNext()) {
            val code = iterator.nextChar()

            if (currentText.isNotEmpty()) {
                components += TextComponent(
                    text = currentText.toString(),
                    color = currentColor,
                    styles = EnumSet.copyOf(currentStyles)
                )
                currentText = StringBuilder()
            }

            // §r 重置
            if (code == 'r') {
                currentColor = ""
                currentStyles.clear()
            } else {
                // §0-§f 颜色
                colorMap[code]?.let { currentColor = it }
                // §k-§o 样式
                styleMap[code]?.let { currentStyles += it }
            }
        } else {
            currentText.append(c)
        }
    }

    if (currentText.isNotEmpty()) {
        components += TextComponent(
            text = currentText.toString(),
            color = currentColor,
            styles = EnumSet.copyOf(currentStyles)
        )
    }

    return if (components.size == 1) {
        components[0]
    } else {
        TextComponent(
            text = "",
            color = "",
            styles = EnumSet.noneOf(TextStyle::class.java),
            extra = components
        )
    }
}

private val colorMap = mapOf(
    '0' to "#000000", '1' to "#0000AA", '2' to "#00AA00", '3' to "#00AAAA",
    '4' to "#AA0000", '5' to "#AA00AA", '6' to "#FFAA00", '7' to "#AAAAAA",
    '8' to "#555555", '9' to "#5555FF", 'a' to "#55FF55", 'b' to "#55FFFF",
    'c' to "#FF5555", 'd' to "#FF55FF", 'e' to "#FFFF55", 'f' to "#FFFFFF"
)

private val styleMap = mapOf(
    'l' to TextStyle.BOLD,
    'o' to TextStyle.ITALIC,
    'n' to TextStyle.UNDERLINED,
    'm' to TextStyle.STRIKETHROUGH,
    'k' to TextStyle.OBFUSCATED
)
