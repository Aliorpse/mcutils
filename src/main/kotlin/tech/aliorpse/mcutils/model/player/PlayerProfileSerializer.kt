package tech.aliorpse.mcutils.model.player

import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

public object PlayerProfileSerializer : KSerializer<PlayerProfile> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("PlayerProfile") {
            element<String>("id")
            element<String>("name")
            element<Boolean>("legacy")
            element<String?>("skinUrl", isOptional = true)
            element<String?>("capeUrl", isOptional = true)
            element<SkinModel>("skinModel")
        }

    @Suppress("MagicNumber")
    override fun serialize(encoder: Encoder, value: PlayerProfile) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.id)
            encodeStringElement(descriptor, 1, value.name)
            encodeBooleanElement(descriptor, 2, value.legacy)
            encodeStringElement(descriptor, 3, value.skinUrl ?: "null")
            encodeStringElement(descriptor, 4, value.capeUrl ?: "null")
            encodeSerializableElement(descriptor, 5, SkinModel.serializer(), value.skinModel)
        }
    }

    override fun deserialize(decoder: Decoder): PlayerProfile {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }
        val jsonObj = decoder.decodeJsonElement().jsonObject

        val id = jsonObj["id"]!!.jsonPrimitive.content
        val name = jsonObj["name"]!!.jsonPrimitive.content
        val legacy = jsonObj["legacy"]?.jsonPrimitive?.booleanOrNull ?: false

        val properties = jsonObj["properties"]!!.jsonArray
        val texturesBase64 = properties
            .first { it.jsonObject["name"]!!.jsonPrimitive.content == "textures" }
            .jsonObject["value"]!!.jsonPrimitive.content

        val decodedJson = String(texturesBase64.decodeBase64Bytes())
        val decoded = Json.decodeFromString<DecodedTextures>(decodedJson)

        val skin = decoded.textures["SKIN"]
        val cape = decoded.textures["CAPE"]

        return PlayerProfile(
            id = id,
            name = name,
            legacy = legacy,
            skinUrl = skin?.url,
            capeUrl = cape?.url,
            skinModel = SkinModel.from(skin?.metadata?.model)
        )
    }
}
