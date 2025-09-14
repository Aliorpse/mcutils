package tech.aliorpse.mcutils.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

internal suspend fun <T> withDispatchersIO(block: suspend () -> T): T {
    val currentContext = currentCoroutineContext()
    return if (Dispatchers.IO.isDispatchNeeded(currentContext)) {
        withContext(Dispatchers.IO) { block() }
    } else {
        block()
    }
}
