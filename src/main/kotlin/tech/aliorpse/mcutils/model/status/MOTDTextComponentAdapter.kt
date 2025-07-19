package tech.aliorpse.mcutils.model.status

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class MOTDTextComponentAdapter(
    private val colorAdapter: ColorAdapter
) : JsonAdapter<MOTDTextComponent>() {
    override fun fromJson(reader: JsonReader): MOTDTextComponent? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> MOTDTextComponent(text = reader.nextString())
            JsonReader.Token.BEGIN_OBJECT -> readComponentObject(reader)
            else -> {
                reader.skipValue()
                MOTDTextComponent("")
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: MOTDTextComponent?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        writer.name("text").value(value.text)
        writer.name("color")
        colorAdapter.toJson(writer, value.color)
        writer.name("bold").value(value.bold)
        writer.name("italic").value(value.italic)
        writer.name("underlined").value(value.underlined)
        writer.name("strikethrough").value(value.strikethrough)
        writer.name("obfuscated").value(value.obfuscated)
        if (!value.extra.isNullOrEmpty()) {
            writer.name("extra")
            writer.beginArray()
            value.extra.forEach { toJson(writer, it) }
            writer.endArray()
        }
        writer.endObject()
    }

    private fun readComponentObject(reader: JsonReader): MOTDTextComponent {
        var text = ""
        var color: Color? = Color.Named.WHITE
        var bold = false
        var italic = false
        var underlined = false
        var strikethrough = false
        var obfuscated = false
        var extra: List<MOTDTextComponent> = emptyList()

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "text" -> text = reader.nextString()
                "color" -> color = colorAdapter.fromJson(reader) ?: Color.Named.WHITE
                "bold" -> bold = reader.nextBoolean()
                "italic" -> italic = reader.nextBoolean()
                "underlined" -> underlined = reader.nextBoolean()
                "strikethrough" -> strikethrough = reader.nextBoolean()
                "obfuscated" -> obfuscated = reader.nextBoolean()
                "extra" -> extra = readExtraArray(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return MOTDTextComponent(
            text = text,
            color = color,
            bold = bold,
            italic = italic,
            underlined = underlined,
            strikethrough = strikethrough,
            obfuscated = obfuscated,
            extra = extra
        )
    }

    private fun readExtraArray(reader: JsonReader): List<MOTDTextComponent> {
        val list = mutableListOf<MOTDTextComponent>()
        reader.beginArray()
        while (reader.hasNext()) {
            fromJson(reader)?.let { list.add(it) }
        }
        reader.endArray()
        return list
    }
}
