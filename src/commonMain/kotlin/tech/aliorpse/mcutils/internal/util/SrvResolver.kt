package tech.aliorpse.mcutils.internal.util

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import tech.aliorpse.mcutils.internal.entity.CachedResult
import tech.aliorpse.mcutils.internal.entity.DohAnswer
import tech.aliorpse.mcutils.internal.entity.GoogleDohResponse
import tech.aliorpse.mcutils.internal.entity.SrvRecord
import tech.aliorpse.mcutils.internal.util.HttpClientProvider.httpClient
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("ReturnCount", "MagicNumber")
internal object SrvResolver {
    private val cache = mutableMapOf<String, CachedResult>()

    @OptIn(ExperimentalTime::class)
    suspend fun resolve(name: String): SrvRecord? {
        val now = Clock.System.now().toEpochMilliseconds()
        cleanup(now)

        cache[name]?.let { c ->
            if (c.expireAt > now) {
                return pickWeighted(c.records)
            }
        }

        val answerList = queryDoh(name)
        val records = answerList.mapNotNull { parseSrv(it.data) }

        val ttl = answerList.minOfOrNull { it.ttl ?: 60 } ?: 60
        val expireAt = now + ttl * 1000

        cache[name] = CachedResult(expireAt, records)

        return pickWeighted(records)
    }

    private suspend fun queryDoh(name: String): List<DohAnswer> {
        val url = "https://dns.google/resolve"

        val resp = try {
            httpClient.get(url) {
                url {
                    parameters.append("name", name)
                    parameters.append("type", "SRV")
                }
                header("accept", "application/dns-json")
            }
        } catch (_: Exception) {
            return emptyList()
        }

        if (!resp.status.isSuccess()) return emptyList()

        return try {
            resp.body<GoogleDohResponse>().answer ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseSrv(data: String): SrvRecord? {
        val parts = data.split(" ")
        if (parts.size != 4) return null

        val priority = parts[0].toIntOrNull() ?: return null
        val weight = parts[1].toIntOrNull() ?: return null
        val port = parts[2].toIntOrNull() ?: return null
        val target = parts[3]

        return SrvRecord(target, port, priority, weight)
    }

    private fun pickWeighted(records: List<SrvRecord>): SrvRecord? {
        val minPriority = records.minOfOrNull { it.priority } ?: return null
        val samePriority = records.filter { it.priority == minPriority }
        if (samePriority.isEmpty()) return null

        val totalWeight = samePriority.sumOf { it.weight }
        return if (totalWeight <= 0) {
            samePriority.random()
        } else {
            val r = (1..totalWeight).random()
            var cumulative = 0
            for (srv in samePriority) {
                cumulative += srv.weight
                if (cumulative >= r) return srv
            }
            samePriority.last()
        }
    }

    private fun cleanup(now: Long) {
        val it = cache.entries.iterator()
        while (it.hasNext()) {
            if (it.next().value.expireAt <= now) it.remove()
        }
    }
}
