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
            when (reader.nextName()) {
                "text" -> {
                    val rawText = reader.nextString()
                    val parsed = rawText.toTextComponent()
                    // 如果解析后是单个组件，直接赋值
                    if (parsed.extra.isNullOrEmpty()) {
                        text = parsed.text
                        color = parsed.color
                        styles += parsed.style
                    } else {
                        text = parsed.text
                        color = parsed.color
                        styles += parsed.style
                        extra = parsed.extra
                    }
                }
                "color" -> color = reader.readColor()
                "extra" -> extra = reader.readExtras()
                else -> reader.readStyle()?.let { styles += it }
            }
        }
        reader.endObject()

        return TextComponent(
            text = text,
            color = color,
            style = styles,
            extra = extra
        )
    }

    private fun JsonReader.readStyle(): TextStyle? {
        val style = styleMap[this.nextName()] ?: run {
            this.skipValue()
            return null
        }
        return if (this.nextBoolean()) style else null
    }

    private fun JsonReader.readColor(): String {
        val colorString = this.nextString()
        return colors[colorString] ?: colorString
    }

    private fun JsonReader.readExtras(): List<TextComponent> {
        val list = mutableListOf<TextComponent>()
        this.beginArray()
        while (this.hasNext()) {
            fromJson(this)?.let { list.add(it) }
        }
        this.endArray()
        return list
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
