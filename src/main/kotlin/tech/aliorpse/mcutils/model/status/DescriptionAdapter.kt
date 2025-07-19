package tech.aliorpse.mcutils.model.status

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import tech.aliorpse.mcutils.util.MOTDParser.objToSection
import tech.aliorpse.mcutils.util.MOTDParser.sectionToObj

class DescriptionAdapter(
    private val motdAdapter: JsonAdapter<MOTDTextComponent>
) : JsonAdapter<Description>() {
    override fun fromJson(reader: JsonReader): Description? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> {
                val text = reader.nextString()
                Description(text, sectionToObj(text))
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                val motdComponent = motdAdapter.fromJson(reader) ?: MOTDTextComponent("")
                Description(objToSection(motdComponent), motdComponent)
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
