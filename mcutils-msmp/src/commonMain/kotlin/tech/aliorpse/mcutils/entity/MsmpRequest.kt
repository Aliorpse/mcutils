package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class MsmpRequest(
    val id: Int,
    val method: String,
    val params: JsonElement,
    val jsonrpc: String = "2.0"
)
