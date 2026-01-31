package tech.aliorpse.mcutils.internal.util

@OptIn(ExperimentalWasmJsInterop::class)
internal actual val isBrowser: Boolean =
    js("typeof window !== 'undefined' && typeof window.document !== 'undefined'")
