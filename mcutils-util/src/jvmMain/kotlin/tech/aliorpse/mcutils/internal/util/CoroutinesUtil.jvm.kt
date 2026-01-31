package tech.aliorpse.mcutils.internal.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

public actual val DispatchersIO: CoroutineDispatcher = Dispatchers.IO

public actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T = runBlocking(block = block)
