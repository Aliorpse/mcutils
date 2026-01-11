@file:OptIn(ExperimentalWasmJsInterop::class)

package tech.aliorpse.mcutils.internal.util

import kotlinx.coroutines.await
import tech.aliorpse.mcutils.internal.entity.SrvRecord
import kotlin.js.JsAny
import kotlin.js.Promise

internal external interface NodeSrvRecord : JsAny {
    val name: String
    val port: Int
    val priority: Int
    val weight: Int
}

@JsModule("dns")
internal external object DnsPromises {
    val promises: DnsPromisesApi
}

internal external interface DnsPromisesApi : JsAny {
    fun resolveSrv(name: String): Promise<JsArray<NodeSrvRecord>>
}

internal actual suspend fun lookupSrv(name: String): List<SrvRecord> {
    val promise = DnsPromises.promises.resolveSrv(name)
    val result = promise.await<JsArray<NodeSrvRecord>>()

    val list = mutableListOf<SrvRecord>()
    for (i in 0 until result.length) {
        val item = result[i]!!
        list.add(
            SrvRecord(
                target = item.name,
                port = item.port,
                priority = item.priority,
                weight = item.weight
            )
        )
    }
    return list
}
