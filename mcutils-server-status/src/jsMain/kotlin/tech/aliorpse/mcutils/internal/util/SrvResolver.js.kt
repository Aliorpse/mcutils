package tech.aliorpse.mcutils.internal.util

import kotlinx.coroutines.await
import tech.aliorpse.mcutils.internal.entity.SrvRecord
import kotlin.js.Promise

private val dnsPromises by lazy {
    js("require('dns').promises")
}

internal actual suspend fun lookupSrv(name: String): List<SrvRecord> {
    val promise = dnsPromises.resolveSrv(name) as Promise<Array<dynamic>>
    val result = promise.await()

    return result.map {
        SrvRecord(
            target = it.name as String,
            port = it.port as Int,
            priority = it.priority as Int,
            weight = it.weight as Int,
        )
    }
}
