package tech.aliorpse.mcutils.model.server

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import tech.aliorpse.mcutils.modules.server.JavaServer.moshi
import tech.aliorpse.mcutils.utils.MOTDParser.objToSection
import tech.aliorpse.mcutils.utils.MOTDParser.sectionToObj

/**
 * Adapt different kinds of Description server sent. String or TextComponent.
 */
internal class DescriptionAdapter(
    private val motdAdapter: MOTDTextComponentAdapter
) {
    private val defaultAdapter by lazy { moshi.adapter(Description::class.java) }

    @FromJson
    fun fromJson(reader: JsonReader): Description? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> {
                val text = reader.nextString()
                Description(text, sectionToObj(text))
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                val motdComponent = motdAdapter.fromJson(reader) ?: MOTDTextComponent("")
                if (motdComponent.text.isNotBlank()) {
                    Description(motdComponent.text, sectionToObj(motdComponent.text))
                } else {
                    Description(objToSection(motdComponent), motdComponent)
                }
            }
            else -> {
                reader.skipValue()
                Description("", MOTDTextComponent(""))
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Description?) {
        defaultAdapter.toJson(writer, value)
    }
}
