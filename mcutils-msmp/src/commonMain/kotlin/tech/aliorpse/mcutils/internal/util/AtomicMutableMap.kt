package tech.aliorpse.mcutils.internal.util

@PublishedApi
internal class AtomicMutableMap<K, V> {
    private val map = mutableMapOf<K, V>()
    private val lock = AtomicSpinLock()

    fun put(key: K, value: V) { lock.withLock { map[key] = value } }

    fun remove(key: K): V? = lock.withLock { map.remove(key) }

    fun get(key: K): V? = lock.withLock { map[key] }

    @PublishedApi
    internal fun getOrPut(key: K, defaultValue: () -> V): V = lock.withLock { map.getOrPut(key, defaultValue) }

    fun <R> withLock(block: (MutableMap<K, V>) -> R): R = lock.withLock { block(map) }

    fun clearAndForEach(block: (K, V) -> Unit) {
        val snapshot = lock.withLock {
            val copy = map.toMap()
            map.clear()
            copy
        }
        snapshot.forEach { (k, v) -> block(k, v) }
    }
}
