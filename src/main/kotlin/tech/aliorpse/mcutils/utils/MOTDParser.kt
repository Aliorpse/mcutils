package tech.aliorpse.mcutils.utils

import tech.aliorpse.mcutils.model.server.Color
import tech.aliorpse.mcutils.model.server.MOTDTextComponent
import tech.aliorpse.mcutils.model.server.colorToOriginalCode

/**
 * Utility functions for MOTD analyzing.
 */
object MOTDParser {
    /**
     * [MOTDTextComponent] to § format.
     * The format output will obey the Java edition's rule.
     * Hex colors will be converted to like §#FF0000.
     *
     * @param component The TextComponent needed to be converted.
     */
    fun objToSection(component: MOTDTextComponent): String {
        val sb = StringBuilder()

        // Process different kinds of color
        val colorCode = when (val c = component.color) {
            is Color.Named -> colorToOriginalCode[c]
            is Color.Custom -> c.hex
            else -> null
        }

        // when text is meaningful
        if (component.text.isNotEmpty()) {
            // `colorCode = null` means inherit
            // skip adding color for blanks
            if (colorCode != null && component.text.isNotBlank()) {
                sb.append("§$colorCode")
            }

            // styles
            if (component.bold) sb.append("§l")
            if (component.italic) sb.append("§o")
            if (component.underlined) sb.append("§n")
            if (component.strikethrough) sb.append("§m")
            if (component.obfuscated) sb.append("§k")

            // plain text
            sb.append(component.text)

            val isStyled = component.bold || component.italic || component.underlined ||
                    component.strikethrough || component.obfuscated
            // when no color but styled then needs a reset
            if (colorCode == null && isStyled) {
                sb.append("§r")
            }
        }

        // rescue
        for (extra in component.extra.orEmpty()) {
            sb.append(objToSection(extra))
        }

        return sb.toString()
    }

    /**
     * § format to [MOTDTextComponent].
     */
    fun sectionToObj(text: String): MOTDTextComponent {
        val components = mutableListOf<MOTDTextComponent>()
        val style = StyleState()
        var currentText = StringBuilder()

        fun flush() {
            if (currentText.isEmpty()) return
            components.add(style.toComponent(currentText.toString()))
            currentText = StringBuilder()
        }

        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '§' && i + 1 < text.length) {
                flush()
                style.updateWithCode(text[i + 1])
                i += 2
            } else {
                currentText.append(c)
                i++
            }
        }

        flush()
        return MOTDTextComponent(text = "", color = null, extra = components)
    }
}

private class StyleState {
    var color: Color? = Color.Named.WHITE
    var bold = false
    var italic = false
    var underlined = false
    var strikethrough = false
    var obfuscated = false

    fun toComponent(text: String) = MOTDTextComponent(
        text = text,
        color = color,
        bold = bold,
        italic = italic,
        underlined = underlined,
        strikethrough = strikethrough,
        obfuscated = obfuscated,
        extra = emptyList()
    )

    fun updateWithCode(code: Char) {
        when (code.lowercaseChar()) {
            in '0'..'9', in 'a'..'f' -> color = Color.Named.fromCode(code)
            'l' -> bold = true
            'o' -> italic = true
            'n' -> underlined = true
            'm' -> strikethrough = true
            'k' -> obfuscated = true
            'r' -> {
                color = Color.Named.WHITE
                bold = false
                italic = false
                underlined = false
                strikethrough = false
                obfuscated = false
            }
        }
    }
}
