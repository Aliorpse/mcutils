package tech.aliorpse.mcutils.model.server

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

internal object JavaServerStatusSerializer : KSerializer<JavaServerStatus> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JavaServerStatus") {
            element<TextComponent>("description")
            element<Players>("players")
            element<Version>("version")
            element<String?>("favicon", isOptional = true)
            element<Boolean>("enforcesSecureChat")
        }

    @Suppress("MagicNumber")
    override fun serialize(encoder: Encoder, value: JavaServerStatus) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(descriptor, 0, TextComponentSerializer, value.description)
        composite.encodeSerializableElement(descriptor, 1, Players.serializer(), value.players)
        composite.encodeSerializableElement(descriptor, 2, Version.serializer(), value.version)
        composite.encodeStringElement(descriptor, 3, value.favicon ?: "null")
        composite.encodeBooleanElement(descriptor, 4, value.enforcesSecureChat)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): JavaServerStatus {
        require(decoder is JsonDecoder)
        val jsonObj = decoder.decodeJsonElement().jsonObject

        val description = decoder.json.decodeFromJsonElement(TextComponentSerializer, jsonObj["description"]!!)
        val players = decoder.json.decodeFromJsonElement(Players.serializer(), jsonObj["players"]!!)
        val version = decoder.json.decodeFromJsonElement(Version.serializer(), jsonObj["version"]!!)
        val favicon = jsonObj["favicon"]?.jsonPrimitive?.contentOrNull
        val enforcesSecureChat = jsonObj["enforcesSecureChat"]?.jsonPrimitive?.booleanOrNull ?: false

        return JavaServerStatus(
            description = description,
            players = players,
            version = version,
            ping = null,
            favicon = favicon,
            enforcesSecureChat = enforcesSecureChat
        )
    }
}
