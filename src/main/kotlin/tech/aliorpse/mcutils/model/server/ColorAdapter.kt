package tech.aliorpse.mcutils.model.server

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import tech.aliorpse.mcutils.model.server.Color.Custom
import tech.aliorpse.mcutils.model.server.Color.Named
import tech.aliorpse.mcutils.modules.server.JavaServer.moshi

/**
 * § codes or Hex strings Adapter.
 */
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
