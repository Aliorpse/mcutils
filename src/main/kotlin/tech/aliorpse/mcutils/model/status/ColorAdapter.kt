package tech.aliorpse.mcutils.model.status

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter


class ColorAdapter : JsonAdapter<Color?>() {
    override fun fromJson(reader: JsonReader): Color? {
        val str = reader.nextString()
        return Color.fromString(str)
            ?: throw JsonDataException("Invalid color: $str")
    }

    override fun toJson(writer: JsonWriter, value: Color?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        val out = when (value) {
            is Color.Named -> value.name
            is Color.Custom -> value.hex
        }
        writer.value(out)
    }
}
