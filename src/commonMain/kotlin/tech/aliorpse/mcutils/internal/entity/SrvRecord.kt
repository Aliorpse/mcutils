package tech.aliorpse.mcutils.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal data class SrvRecord(
    val target: String,
    val port: Int,
    val priority: Int,
    val weight: Int,
)

internal data class CachedResult(
    val expireAt: Long,
    val records: List<SrvRecord>
)

@Serializable
internal data class GoogleDohResponse(
    @SerialName("Answer") val answer: List<DohAnswer>? = null
)

@Serializable
internal data class DohAnswer(
    val data: String,
    @SerialName("TTL") val ttl: Int? = null
)
