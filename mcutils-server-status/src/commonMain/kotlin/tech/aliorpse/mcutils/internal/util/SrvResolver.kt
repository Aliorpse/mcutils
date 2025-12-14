package tech.aliorpse.mcutils.internal.util

import tech.aliorpse.mcutils.internal.entity.SrvRecord

internal expect suspend fun lookupSrv(name: String): List<SrvRecord>

@Suppress("ReturnCount", "MagicNumber")
internal object SrvResolver {
    suspend fun resolve(name: String): SrvRecord? {
        return pickWeighted(lookupSrv(name))
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
}
