package tech.aliorpse.mcutils.internal.util

@OptIn(ExperimentalWasmJsInterop::class)
internal actual val isBrowser: Boolean
    get() = js("typeof window !== 'undefined' && typeof window.document !== 'undefined'")
