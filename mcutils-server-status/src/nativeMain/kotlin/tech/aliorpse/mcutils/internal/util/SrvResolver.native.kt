package tech.aliorpse.mcutils.internal.util

import tech.aliorpse.mcutils.internal.entity.SrvRecord

internal actual suspend fun lookupSrv(name: String): List<SrvRecord> {
    // To be implemented
    return listOf(SrvRecord(name, 25565, 0, 0))
}
