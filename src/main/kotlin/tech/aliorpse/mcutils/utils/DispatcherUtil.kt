package tech.aliorpse.mcutils.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

suspend fun <T> withDispatcherIO(block: suspend () -> T): T {
    val currentContext: CoroutineContext = currentCoroutineContext()
    return if (Dispatchers.IO.isDispatchNeeded(currentContext)) {
        withContext(Dispatchers.IO) { block() }
    } else {
        block()
    }
}
