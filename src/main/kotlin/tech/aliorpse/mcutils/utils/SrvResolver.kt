package tech.aliorpse.mcutils.utils

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.aliorpse.mcutils.utils.McUtilsHttpClientProvider.httpClient
import java.util.concurrent.ConcurrentHashMap

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

private data class CachedSrv(val host: String, val port: Int, val expireAt: Long)

private val srvCache = ConcurrentHashMap<String, CachedSrv>()

@Suppress("MagicNumber")
internal suspend fun defaultResolveSrvRecord(host: String): Pair<String, Int>? {
    val now = System.currentTimeMillis()

    srvCache[host]?.let { cached ->
        if (cached.expireAt > now) return cached.host to cached.port
        else srvCache.remove(host)
    }

    val url = "https://dns.google/resolve?name=_minecraft._tcp.$host&type=SRV"
    val response: DnsResponse = httpClient.get(url).body()

    val srv = response.answer?.firstOrNull { it.type == 33 }
    val parts = srv?.data?.split(" ")

    val result = parts
        ?.takeIf { it.size >= 4 }
        ?.getOrNull(2)?.toIntOrNull()
        ?.let { port ->
            parts[3].removeSuffix(".") to port
        }

    if (result != null) {
        val expireAt = now + srv.ttl * 1000L
        srvCache[host] = CachedSrv(result.first, result.second, expireAt)
    }

    return result
}
