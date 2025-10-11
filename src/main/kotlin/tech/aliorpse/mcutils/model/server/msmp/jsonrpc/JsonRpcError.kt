package tech.aliorpse.mcutils.model.server.msmp.jsonrpc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * JSON-RPC error object
 */
@Serializable
public data class JsonRpcError(
    public val code: JsonRpcErrorCode,
    public val message: String,
    public val data: String,
)

@Suppress("MagicNumber")
@Serializable(with = JsonRpcErrorCode.Serializer::class)
public enum class JsonRpcErrorCode(public val numeric: Int) {
    ParseError(-32700),
    InvalidRequest(-32600),
    MethodNotFound(-32601),
    InvalidParams(-32602),
    InternalError(-32603);

    internal object Serializer : KSerializer<JsonRpcErrorCode> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("JsonRpcErrorCode", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: JsonRpcErrorCode) {
            encoder.encodeInt(value.numeric)
        }

        override fun deserialize(decoder: Decoder): JsonRpcErrorCode {
            val code = decoder.decodeInt()
            return entries.find { it.numeric == code } ?: error("Server returned invalid error code: $code")
        }
    }

    public companion object {
        public fun fromCode(code: Int): JsonRpcErrorCode =
            entries.find { it.numeric == code } ?: error("Server returned invalid error code: $code")
    }
}
