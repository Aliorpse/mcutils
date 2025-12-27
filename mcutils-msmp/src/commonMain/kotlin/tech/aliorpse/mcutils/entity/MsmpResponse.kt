package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class MsmpResponse(
    val jsonrpc: String = "2.0",
    val id: Int,
    val result: JsonElement? = null,
    val error: MsmpResponseError? = null
)

@Serializable
public data class MsmpResponseError(
    val code: Int,
    val message: String,
    val data: String? = null
)
