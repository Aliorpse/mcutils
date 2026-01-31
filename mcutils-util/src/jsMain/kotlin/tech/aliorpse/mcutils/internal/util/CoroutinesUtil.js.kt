package tech.aliorpse.mcutils.internal.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

public actual val DispatchersIO: CoroutineDispatcher = Dispatchers.Unconfined

@OptIn(DelicateCoroutinesApi::class)
public actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T =
    GlobalScope.async { block(this) }.asDynamic()
