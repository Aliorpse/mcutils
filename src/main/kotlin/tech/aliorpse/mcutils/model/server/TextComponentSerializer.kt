package tech.aliorpse.mcutils.model.server

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.collections.emptyList
import kotlin.let

internal object TextComponentSerializer : KSerializer<TextComponent> {
    private val extraSerializer by lazy { ListSerializer(TextComponentSerializer) }

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("TextComponent") {
            element<String>("text")
            element<String>("color")
            element<Set<TextStyle>>("styles")
            element<List<TextComponent>>("extra", isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: TextComponent) {
        require(encoder is JsonEncoder)
        val obj = buildJsonObject {
            put("text", value.text)
            put("color", value.color)
            put("styles", encoder.json.encodeToJsonElement(SetSerializer(TextStyle.serializer()), value.styles))
            if (value.extra.isNotEmpty()) {
                put("extra", encoder.json.encodeToJsonElement(extraSerializer, value.extra))
            }
        }
        encoder.encodeJsonElement(obj)
    }

    override fun deserialize(decoder: Decoder): TextComponent {
        require(decoder is JsonDecoder)
        val json = decoder.decodeJsonElement().jsonObject

        val text = json["text"]?.jsonPrimitive?.content ?: ""
        val color = json["color"]?.jsonPrimitive?.content ?: ""
        val styles = json["styles"]?.let {
            decoder.json.decodeFromJsonElement(SetSerializer(TextStyle.serializer()), it)
        } ?: emptySet()
        val extra = json["extra"]?.let {
            // 在这里使用 lazy 初始化的 extraSerializer
            decoder.json.decodeFromJsonElement(extraSerializer, it)
        } ?: emptyList()

        return TextComponent(text, color, styles, extra)
    }
}
