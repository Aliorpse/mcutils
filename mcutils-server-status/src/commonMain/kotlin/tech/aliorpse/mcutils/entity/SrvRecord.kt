package tech.aliorpse.mcutils.entity

public data class SrvRecord(
    val target: String,
    val port: Int,
    val priority: Int,
    val weight: Int,
)
