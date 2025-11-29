package tech.aliorpse.mcutils.internal.util

import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

internal expect val DispatchersIO: CoroutineDispatcher

internal val globalSelectorIO = SelectorManager(DispatchersIO)

internal suspend fun <T> withDispatchersIO(block: suspend () -> T): T {
    val currentContext = currentCoroutineContext()
    return if (DispatchersIO.isDispatchNeeded(currentContext)) {
        withContext(DispatchersIO) { block() }
    } else {
        block()
    }
}
