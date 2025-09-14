package tech.aliorpse.mcutils.model.server

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import tech.aliorpse.mcutils.utils.toTextComponent
import java.util.EnumSet

internal object TextComponentSerializer : KSerializer<TextComponent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TextComponent")

    override fun serialize(encoder: Encoder, value: TextComponent) {
        require(encoder is JsonEncoder)

        val obj = buildJsonObject {
            put("text", value.text)
            if (value.color.isNotEmpty()) put("color", value.color)

            styleMap.forEach { (name, style) ->
                if (value.styles.contains(style)) put(name, true)
            }

            if (value.extra.isNotEmpty()) {
                put(
                    "extra",
                    encoder.json.encodeToJsonElement(ListSerializer(TextComponentSerializer), value.extra)
                )
            }
        }

        encoder.encodeJsonElement(obj)
    }

    override fun deserialize(decoder: Decoder): TextComponent {
        require(decoder is JsonDecoder)
        return when (val element = decoder.decodeJsonElement()) {
            is JsonPrimitive -> { element.content.toTextComponent() }

            is JsonObject -> {
                var text = ""
                var color = ""
                val styles: EnumSet<TextStyle> = EnumSet.noneOf(TextStyle::class.java)
                var extra: List<TextComponent> = emptyList()

                element.forEach { (name, value) ->
                    when (name) {
                        "text" -> {
                            val rawText = when (value) {
                                is JsonPrimitive -> value.content
                                is JsonObject -> decoder.json.decodeFromJsonElement(TextComponentSerializer, value).text
                                else -> value.toString()
                            }

                            val parsed = rawText.toTextComponent()
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
                            color = colors[colorName] ?: colorName
                        }

                        "extra" -> {
                            if (value is JsonArray) {
                                extra = decoder.json.decodeFromJsonElement(
                                    ListSerializer(TextComponentSerializer), value
                                )
                            }
                        }

                        in styleMap.keys -> {
                            if (value is JsonPrimitive && value.booleanOrNull == true) {
                                styles += styleMap.getValue(name)
                            }
                        }

                        else -> {}
                    }
                }
                TextComponent(text, color, styles, extra)
            }
            else -> {
                TextComponent("", "#FFFFFF")
            }
        }
    }

    private val styleMap = mapOf(
        "bold" to TextStyle.BOLD,
        "italic" to TextStyle.ITALIC,
        "underlined" to TextStyle.UNDERLINED,
        "strikethrough" to TextStyle.STRIKETHROUGH,
        "obfuscated" to TextStyle.OBFUSCATED,
    )

    private val colors = mapOf(
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
}
