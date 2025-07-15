package tech.aliorpse.mcutils.util

import tech.aliorpse.mcutils.model.status.MOTDTextComponent

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
    fun objectToSectionFormat(component: MOTDTextComponent): String {
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
            sb.append(objectToSectionFormat(extra))
        }

        return sb.toString()
    }

    /**
     * § format to [MOTDTextComponent].
     */
    @Suppress("AssignedValueIsNeverRead")
    fun sectionFormatToObject(text: String): MOTDTextComponent {
        val components = mutableListOf<MOTDTextComponent>()

        var currentText = StringBuilder()

        var currentColor: Color? = Color.Named.WHITE
        var bold = false
        var italic = false
        var underlined = false
        var strikethrough = false
        var obfuscated = false

        fun flushComponent() {
            if (currentText.isEmpty()) return
            components.add(
                MOTDTextComponent(
                    text = currentText.toString(),
                    color = currentColor,
                    bold = bold,
                    italic = italic,
                    underlined = underlined,
                    strikethrough = strikethrough,
                    obfuscated = obfuscated,
                    extra = emptyList()
                )
            )
            currentText = StringBuilder()
        }

        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '§' && i + 1 < text.length) {
                flushComponent()

                val code = text[i + 1].lowercaseChar()
                when (code) {
                    in '0'..'9' -> currentColor = Color.Named.entries.toTypedArray()[code - '0']
                    'a' -> currentColor = Color.Named.GREEN
                    'b' -> currentColor = Color.Named.AQUA
                    'c' -> currentColor = Color.Named.RED
                    'd' -> currentColor = Color.Named.LIGHT_PURPLE
                    'e' -> currentColor = Color.Named.YELLOW
                    'f' -> currentColor = Color.Named.WHITE
                    '1' -> currentColor = Color.Named.DARK_BLUE
                    '2' -> currentColor = Color.Named.DARK_GREEN
                    '3' -> currentColor = Color.Named.DARK_AQUA
                    '4' -> currentColor = Color.Named.DARK_RED
                    '5' -> currentColor = Color.Named.DARK_PURPLE
                    '6' -> currentColor = Color.Named.GOLD
                    '7' -> currentColor = Color.Named.GRAY
                    '8' -> currentColor = Color.Named.DARK_GRAY
                    '9' -> currentColor = Color.Named.BLUE

                    'l' -> bold = true
                    'o' -> italic = true
                    'n' -> underlined = true
                    'm' -> strikethrough = true
                    'k' -> obfuscated = true

                    'r' -> {
                        currentColor = Color.Named.WHITE
                        bold = false
                        italic = false
                        underlined = false
                        strikethrough = false
                        obfuscated = false
                    }
                }
                i += 2
                continue
            }

            currentText.append(c)
            i++
        }

        flushComponent()

        return MOTDTextComponent(
            text = "",
            color = null,
            extra = components
        )
    }
}
