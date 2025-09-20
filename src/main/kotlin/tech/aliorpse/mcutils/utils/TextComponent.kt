package tech.aliorpse.mcutils.utils

import tech.aliorpse.mcutils.model.server.TextComponent
import tech.aliorpse.mcutils.model.server.TextStyle
import java.util.*

/**
 * Coverts string with "§" to a [TextComponent].
 */
public fun String.toTextComponent(): TextComponent {
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
                    text = currentText.toString(), color = currentColor, styles = EnumSet.copyOf(currentStyles)
                )
                currentText = StringBuilder()
            }

            // §r reset
            if (code == 'r') {
                currentColor = ""
                currentStyles.clear()
            } else {
                // §0-§f colors
                originalColorMap[code]?.let { currentColor = it }
                // §k-§o styles
                originalStyleMap[code]?.let { currentStyles += it }
            }
        } else {
            currentText.append(c)
        }
    }

    if (currentText.isNotEmpty()) {
        components += TextComponent(
            text = currentText.toString(), color = currentColor, styles = EnumSet.copyOf(currentStyles)
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

private val originalColorMap = mapOf(
    '0' to "#000000",
    '1' to "#0000AA",
    '2' to "#00AA00",
    '3' to "#00AAAA",
    '4' to "#AA0000",
    '5' to "#AA00AA",
    '6' to "#FFAA00",
    '7' to "#AAAAAA",
    '8' to "#555555",
    '9' to "#5555FF",
    'a' to "#55FF55",
    'b' to "#55FFFF",
    'c' to "#FF5555",
    'd' to "#FF55FF",
    'e' to "#FFFF55",
    'f' to "#FFFFFF"
)

private val originalStyleMap = mapOf(
    'l' to TextStyle.BOLD,
    'o' to TextStyle.ITALIC,
    'n' to TextStyle.UNDERLINED,
    'm' to TextStyle.STRIKETHROUGH,
    'k' to TextStyle.OBFUSCATED
)

/**
 * Converts [TextComponent] to a HTML element.
 */
public fun TextComponent.toHtml(): String {
    var html = escapeHtml(text).replace(" ", "&nbsp;").replace("\n", "<br />")

    if (extra.isNotEmpty()) {
        html += extra.joinToString("") { it.toHtml() }
    }

    if (TextStyle.OBFUSCATED in styles) html = """<span class="obfuscated">$html</span>"""
    if (TextStyle.STRIKETHROUGH in styles) html = "<s>$html</s>"
    if (TextStyle.UNDERLINED in styles) html = "<u>$html</u>"
    if (TextStyle.ITALIC in styles) html = "<i>$html</i>"
    if (TextStyle.BOLD in styles) html = "<b>$html</b>"

    return if (color.isNotEmpty()) {
        """<span style="color:$color">$html</span>"""
    } else {
        html
    }
}

private fun escapeHtml(text: String): String = buildString {
    for (c in text) {
        when (c) {
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '&' -> append("&amp;")
            '"' -> append("&quot;")
            '\'' -> append("&#39;")
            else -> append(c)
        }
    }
}
