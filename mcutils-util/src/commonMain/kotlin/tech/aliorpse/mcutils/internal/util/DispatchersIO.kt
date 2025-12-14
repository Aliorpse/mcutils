package tech.aliorpse.mcutils.internal.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

public expect val DispatchersIO: CoroutineDispatcher

public suspend fun <T> withDispatchersIO(block: suspend () -> T): T {
    val currentContext = currentCoroutineContext()
    return if (DispatchersIO.isDispatchNeeded(currentContext)) {
        withContext(DispatchersIO) { block() }
    } else {
        block()
    }
}
