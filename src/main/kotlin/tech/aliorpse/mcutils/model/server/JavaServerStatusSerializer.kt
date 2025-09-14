package tech.aliorpse.mcutils.model.server

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal object JavaServerStatusSerializer : KSerializer<JavaServerStatus> {
    override val descriptor: SerialDescriptor by lazy {
        buildClassSerialDescriptor("JavaServerStatus") {
            element<String>("description")
            element<Players>("players")
            element<Version>("version")
            element<String>("favicon", isOptional = true)
            element<Boolean>("enforcesSecureChat", isOptional = true)
        }
    }

    override fun serialize(encoder: Encoder, value: JavaServerStatus) {
        require(encoder is JsonEncoder)
        val obj = buildJsonObject {
            put("description", encoder.json.encodeToJsonElement(TextComponentSerializer, value.description))
            put("players", encoder.json.encodeToJsonElement(Players.serializer(), value.players))
            put("version", encoder.json.encodeToJsonElement(Version.serializer(), value.version))
            value.favicon?.let { put("favicon", it) }
            put("enforcesSecureChat", value.enforcesSecureChat)
        }
        encoder.encodeJsonElement(obj)
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
