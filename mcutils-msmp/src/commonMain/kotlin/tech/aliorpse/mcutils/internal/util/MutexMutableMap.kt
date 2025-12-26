package tech.aliorpse.mcutils.internal.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@PublishedApi
internal class MutexMutableMap<K, V> {
    @PublishedApi
    internal val mutex = Mutex()

    @PublishedApi
    internal val map = mutableMapOf<K, V>()

    suspend fun put(key: K, value: V) = mutex.withLock {
        map[key] = value
    }

    suspend fun remove(key: K): V? = mutex.withLock {
        map.remove(key)
    }

    suspend fun get(key: K): V? = mutex.withLock {
        map[key]
    }

    suspend fun <T> withLock(block: (MutableMap<K, V>) -> T): T = mutex.withLock {
        block(map)
    }

    suspend inline fun forEach(crossinline block: (K, V) -> Unit) {
        val snapshot = mutex.withLock { map.toMap() }
        snapshot.forEach { (k, v) -> block(k, v) }
    }

    suspend inline fun clearAndForEach(crossinline block: (K, V) -> Unit) {
        val snapshot = mutex.withLock {
            val copy = map.toMap()
            map.clear()
            copy
        }
        snapshot.forEach { (k, v) -> block(k, v) }
    }
}
