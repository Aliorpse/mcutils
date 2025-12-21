package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable

@Serializable
public data class MsmpRequest<T>(
    val id: Int,
    val method: String,
    val params: T? = null,
    val jsonrpc: String = "2.0"
)
