package tech.aliorpse.mcutils.model.status

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import tech.aliorpse.mcutils.model.status.Color.Custom
import tech.aliorpse.mcutils.model.status.Color.Named
import tech.aliorpse.mcutils.module.status.JavaPing.moshi


class ColorAdapter {
    private val defaultAdapter by lazy { moshi.adapter(Color::class.java) }

    @FromJson
    fun fromJson(reader: JsonReader): Color? {
        val str = reader.nextString()
        return Named.fromName(str)
            ?: if (str.matches(Regex("^#[0-9a-fA-F]{6}$"))) Custom(str.lowercase())
            else null
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Color?) {
        defaultAdapter.toJson(writer, value)
    }
}
