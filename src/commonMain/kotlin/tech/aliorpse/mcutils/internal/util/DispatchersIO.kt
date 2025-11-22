package tech.aliorpse.mcutils.internal.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

// To be implemented
internal val DispatchersIO = Dispatchers.Default

internal suspend fun <T> withDispatchersIO(block: suspend () -> T): T {
    val currentContext = currentCoroutineContext()
    return if (DispatchersIO.isDispatchNeeded(currentContext)) {
        withContext(DispatchersIO) { block() }
    } else {
        block()
    }
}
