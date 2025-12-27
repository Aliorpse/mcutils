package tech.aliorpse.mcutils.internal.util

import kotlin.collections.emptyMap
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@PublishedApi
@OptIn(ExperimentalAtomicApi::class)
internal class AtomicCopyOnWriteMap<K, V> {
    @PublishedApi
    internal val atomicMap = AtomicReference<Map<K, V>>(emptyMap())

    fun get(key: K): V? = atomicMap.load()[key]

    fun put(key: K, value: V) {
        while (true) {
            val oldMap = atomicMap.load()
            val newMap = oldMap.toMutableMap()
            newMap[key] = value
            if (atomicMap.compareAndSet(oldMap, newMap)) break
        }
    }

    fun remove(key: K): V? {
        while (true) {
            val oldMap = atomicMap.load()
            if (!oldMap.containsKey(key)) return null

            val newMap = oldMap.toMutableMap()
            val removedValue = newMap.remove(key)
            if (atomicMap.compareAndSet(oldMap, newMap)) {
                return removedValue
            }
        }
    }

    fun update(block: (MutableMap<K, V>) -> Unit) {
        while (true) {
            val oldMap = atomicMap.load()
            val newMap = oldMap.toMutableMap()
            block(newMap)
            if (atomicMap.compareAndSet(oldMap, newMap)) break
        }
    }

    @Suppress("ReturnCount")
    fun getOrPut(key: K, defaultValue: () -> V): V {
        val fastCheck = atomicMap.load()[key]
        if (fastCheck != null) return fastCheck

        val newValue = defaultValue()

        while (true) {
            val oldMap = atomicMap.load()

            val existing = oldMap[key]
            if (existing != null) return existing

            val newMap = oldMap.toMutableMap()
            newMap[key] = newValue

            if (atomicMap.compareAndSet(oldMap, newMap)) {
                return newValue
            }
        }
    }

    inline fun forEach(block: (K, V) -> Unit) {
        atomicMap.load().forEach { (k, v) -> block(k, v) }
    }
}
