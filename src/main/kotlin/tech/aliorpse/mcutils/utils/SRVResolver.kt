package tech.aliorpse.mcutils.utils

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.aliorpse.mcutils.utils.McUtilsHttpClientProvider.httpClient

@Serializable
private data class DnsResponse(
    @SerialName("Answer") val answer: List<DnsAnswer>? = null
)

@Serializable
private data class DnsAnswer(
    val name: String,
    val type: Int,
    @SerialName("TTL") val ttl: Int,
    val data: String
)

@Suppress("MagicNumber")
internal suspend fun resolveSrvRecord(host: String): Pair<String, Int>? {
    val url = "https://dns.google/resolve?name=_minecraft._tcp.$host&type=SRV"
    val response: DnsResponse = httpClient.get(url).body()

    val srv = response.answer?.firstOrNull { it.type == 33 }
    val parts = srv?.data?.split(" ")

    return parts
        ?.takeIf { it.size >= 4 }
        ?.getOrNull(2)?.toIntOrNull()
        ?.let { port ->
            parts[3].removeSuffix(".") to port
        }
}
