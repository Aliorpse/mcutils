package tech.aliorpse.mcutils.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import tech.aliorpse.mcutils.entity.TextComponent
import tech.aliorpse.mcutils.entity.TextStyle
import kotlin.collections.component1
import kotlin.collections.component2

public fun TextComponent.Companion.fromJson(element: String): TextComponent =
    fromJson(Json.parseToJsonElement(element))

public fun TextComponent.Companion.fromJson(element: JsonElement): TextComponent {
    return when (element) {
        is JsonPrimitive -> TextComponent.fromString(element.content)

        is JsonObject -> {
            var text = ""
            var color = ""
            val styles: MutableSet<TextStyle> = mutableSetOf()
            var extra: List<TextComponent> = emptyList()

            element.forEach { (name, value) ->
                when (name) {
                    "text" -> {
                        val rawText = when (value) {
                            is JsonPrimitive -> value.content
                            is JsonObject -> fromJson(value).text
                            else -> value.toString()
                        }

                        val parsed = TextComponent.fromString(rawText)
                        if ("§" !in rawText) {
                            text = rawText
                        } else {
                            text = parsed.text
                            color = parsed.color
                            styles += parsed.styles
                            if (parsed.extra.isNotEmpty()) extra = parsed.extra
                        }
                    }

                    "color" -> {
                        val colorName = if (value is JsonPrimitive) value.content else value.toString()
                        color = namedColorMap[colorName] ?: colorName
                    }

                    "extra" -> {
                        (value as? JsonArray)?.let { array ->
                            extra = array.map { fromJson(it) }
                        }
                    }

                    in namedStyleMap.keys -> {
                        if (value is JsonPrimitive && value.booleanOrNull == true) {
                            styles += namedStyleMap.getValue(name)
                        }
                    }
                }
            }
            TextComponent(text, color, styles, extra)
        }

        else -> {
            TextComponent("", "#FFFFFF")
        }
    }
}

public fun TextComponent.Companion.fromString(text: String): TextComponent {
    val components = mutableListOf<TextComponent>()
    var currentText = StringBuilder()
    var currentColor = ""
    val currentStyles: MutableSet<TextStyle> = mutableSetOf()

    val iterator = text.iterator()
    while (iterator.hasNext()) {
        val c = iterator.nextChar()
        if (c == '§' && iterator.hasNext()) {
            val code = iterator.nextChar()

            if (currentText.isNotEmpty()) {
                components += TextComponent(
                    text = currentText.toString(), color = currentColor, styles = currentStyles.toSet()
                )
                currentText = StringBuilder()
            }

            // §r reset
            if (code == 'r') {
                currentColor = ""
                currentStyles.clear()
            } else {
                // §0-§f colors
                numericColorMap[code]?.let { currentColor = it }
                // §k-§o styles
                simplifiedStyleMap[code]?.let { currentStyles += it }
            }
        } else {
            currentText.append(c)
        }
    }

    if (currentText.isNotEmpty()) {
        components += TextComponent(
            text = currentText.toString(), color = currentColor, styles = currentStyles.toSet()
        )
    }

    return if (components.size == 1) {
        components[0]
    } else {
        TextComponent(
            text = "",
            color = "",
            styles = emptySet(),
            extra = components
        )
    }
}

private val namedColorMap = mapOf(
    "black" to "#000000",
    "dark_blue" to "#0000AA",
    "dark_green" to "#00AA00",
    "dark_aqua" to "#00AAAA",
    "dark_red" to "#AA0000",
    "dark_purple" to "#AA00AA",
    "gold" to "#FFAA00",
    "gray" to "#AAAAAA",
    "dark_gray" to "#555555",
    "blue" to "#5555FF",
    "green" to "#55FF55",
    "aqua" to "#55FFFF",
    "red" to "#FF5555",
    "light_purple" to "#FF55FF",
    "yellow" to "#FFFF55",
    "white" to "#FFFFFF"
)

private val numericColorMap = mapOf(
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

private val namedStyleMap = mapOf(
    "bold" to TextStyle.BOLD,
    "italic" to TextStyle.ITALIC,
    "underlined" to TextStyle.UNDERLINED,
    "strikethrough" to TextStyle.STRIKETHROUGH,
    "obfuscated" to TextStyle.OBFUSCATED,
)

private val simplifiedStyleMap = mapOf(
    'l' to TextStyle.BOLD,
    'o' to TextStyle.ITALIC,
    'n' to TextStyle.UNDERLINED,
    'm' to TextStyle.STRIKETHROUGH,
    'k' to TextStyle.OBFUSCATED
)

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

public fun TextComponent.toPlainText(): String {
    val extraText = extra.joinToString(separator = "") { it.toPlainText() }
    return text + extraText
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
