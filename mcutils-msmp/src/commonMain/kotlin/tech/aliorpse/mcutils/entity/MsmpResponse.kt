package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable

@Serializable
public data class MsmpResponse<T>(
    val jsonrpc: String = "2.0",
    val id: Int,
    val result: T? = null,
    val error: MsmpError? = null
)

@Serializable
public data class MsmpError(
    val code: Int,
    val message: String,
    val data: String? = null
)
