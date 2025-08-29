package tech.aliorpse.mcutils.model.server

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import tech.aliorpse.mcutils.modules.server.JavaServer.moshi
import tech.aliorpse.mcutils.utils.toTextComponent
import java.util.EnumSet

internal class TextComponentAdapter {
    private val defaultAdapter by lazy { moshi.adapter(TextComponent::class.java) }

    @FromJson
    fun fromJson(reader: JsonReader): TextComponent? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString().toTextComponent()
            JsonReader.Token.BEGIN_OBJECT -> readComponentObject(reader)
            else -> {
                reader.skipValue()
                TextComponent("", "#FFFFFF")
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: TextComponent?) {
        defaultAdapter.toJson(writer, value)
    }

    private fun readComponentObject(reader: JsonReader): TextComponent {
        var text = ""
        var color = ""
        val styles: EnumSet<TextStyle> = EnumSet.noneOf(TextStyle::class.java)
        var extra: List<TextComponent> = emptyList()

        reader.beginObject()
        while (reader.hasNext()) {
            when (val name = reader.nextName()) {
                "text" -> {
                    val rawText = reader.nextString()
                    val parsed = rawText.toTextComponent()

                    text = rawText.takeIf { "§" !in rawText } ?: parsed.text.also {
                        color = parsed.color
                        styles += parsed.styles
                        if (parsed.extra.isNotEmpty()) extra = parsed.extra
                    }
                }
                "color" -> color = reader.readColor()
                "extra" -> extra = reader.readExtras()
                in stylesList -> reader.readStyle(name)?.let { styles += it }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return TextComponent(
            text = text,
            color = color,
            styles = styles,
            extra = extra
        )
    }

    private fun JsonReader.readStyle(name: String): TextStyle? {
        return if (nextBoolean()) styleMap[name] else null
    }

    private fun JsonReader.readColor(): String {
        val colorString = nextString()
        return colors[colorString] ?: colorString
    }

    private fun JsonReader.readExtras(): List<TextComponent> {
        val list = mutableListOf<TextComponent>()
        beginArray()
        while (hasNext()) {
            fromJson(this)?.let { list.add(it) }
        }
        endArray()
        return list
    }

    private val styleMap = mapOf(
        "bold" to TextStyle.BOLD,
        "italic" to TextStyle.ITALIC,
        "underlined" to TextStyle.UNDERLINED,
        "strikethrough" to TextStyle.STRIKETHROUGH,
        "obfuscated" to TextStyle.OBFUSCATED,
    )

    private val stylesList = listOf("bold", "italic", "underlined", "strikethrough", "obfuscated")

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
