package tech.aliorpse.mcutils.model.status

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import tech.aliorpse.mcutils.module.status.JavaPing.moshi

class MOTDTextComponentAdapter(
    private val colorAdapter: ColorAdapter
) {
    private val defaultAdapter by lazy { moshi.adapter(MOTDTextComponent::class.java) }

    @FromJson
    fun fromJson(reader: JsonReader): MOTDTextComponent? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> MOTDTextComponent(text = reader.nextString())
            JsonReader.Token.BEGIN_OBJECT -> readComponentObject(reader)
            else -> {
                reader.skipValue()
                MOTDTextComponent("")
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: MOTDTextComponent?) {
        defaultAdapter.toJson(writer, value)
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
