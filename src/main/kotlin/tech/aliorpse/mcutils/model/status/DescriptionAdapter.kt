package tech.aliorpse.mcutils.model.status

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import tech.aliorpse.mcutils.util.MOTDParser.objectToSectionFormat
import tech.aliorpse.mcutils.util.MOTDParser.sectionFormatToObject

class DescriptionAdapter(
    private val motdAdapter: JsonAdapter<MOTDTextComponent>
) : JsonAdapter<Description>() {
    override fun fromJson(reader: JsonReader): Description? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> {
                val text = reader.nextString()
                Description(text, sectionFormatToObject(text))
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                val motdComponent = motdAdapter.fromJson(reader) ?: MOTDTextComponent("")
                Description(objectToSectionFormat(motdComponent), motdComponent)
            }
            else -> {
                reader.skipValue()
                Description("", MOTDTextComponent(""))
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: Description?) {
        throw NotImplementedError()
    }
}
