package tech.aliorpse.mcutils.internal.util

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
internal class AtomicSpinLock {
    private val state = AtomicBoolean(false)

    fun lock() { while (!state.compareAndSet(expectedValue = false, newValue = true)) {} }

    fun unlock() = state.store(true)

    inline fun <T> withLock(block: () -> T): T {
        lock()
        try { return block() } finally { unlock() }
    }
}
