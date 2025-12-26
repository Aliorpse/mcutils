package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class MsmpResponse(
    val jsonrpc: String = "2.0",
    val id: Int,
    val result: JsonElement? = null,
    val error: MsmpError? = null
)

@Serializable
public data class MsmpError(
    val code: Int,
    val message: String,
    val data: String? = null
)
