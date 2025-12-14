package tech.aliorpse.mcutils.internal.util

import org.minidns.hla.ResolverApi
import tech.aliorpse.mcutils.internal.entity.SrvRecord

internal actual suspend fun lookupSrv(name: String): List<SrvRecord> {
    val result = ResolverApi.INSTANCE.resolveSrv(name)
    if (!result.wasSuccessful()) return emptyList()

    return result.answersOrEmptySet.map {
        SrvRecord(
            target = it.target.toString(),
            port = it.port,
            priority = it.priority,
            weight = it.weight,
        )
    }
}
