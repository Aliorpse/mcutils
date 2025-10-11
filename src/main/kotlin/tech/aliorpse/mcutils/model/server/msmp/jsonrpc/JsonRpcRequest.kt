package tech.aliorpse.mcutils.model.server.msmp.jsonrpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * JSON-RPC request
 */
@Serializable
internal data class JsonRpcRequest(
    val id: Int,
    val method: String,
    val params: JsonElement,
    @SerialName("jsonrpc")
    val jsonRpc: String = "2.0"
)
