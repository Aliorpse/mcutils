package tech.aliorpse.mcutils.internal.util

import kotlinx.coroutines.*

public actual val DispatchersIO: CoroutineDispatcher = Dispatchers.Unconfined

@OptIn(DelicateCoroutinesApi::class)
public actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T =
    GlobalScope.async { block(this) }.asDynamic()
