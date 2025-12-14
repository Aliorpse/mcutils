package tech.aliorpse.mcutils.internal.entity

internal data class SrvRecord(
    val target: String,
    val port: Int,
    val priority: Int,
    val weight: Int,
)
