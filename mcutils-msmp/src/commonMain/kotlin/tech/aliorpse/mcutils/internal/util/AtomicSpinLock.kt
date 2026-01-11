package tech.aliorpse.mcutils.internal.util

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
internal class AtomicSpinLock {
    // 0 = unlocked, 1 = locked
    private val state = AtomicInt(0)

    fun lock() { while (!state.compareAndSet(0, 1)) {} }

    fun unlock() = state.store(0)

    inline fun <T> withLock(block: () -> T): T {
        lock()
        try { return block() } finally { unlock() }
    }
}
