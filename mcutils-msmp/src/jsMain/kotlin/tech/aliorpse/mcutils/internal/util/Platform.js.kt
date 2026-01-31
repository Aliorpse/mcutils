package tech.aliorpse.mcutils.internal.util

internal actual val isBrowser: Boolean =
    js("typeof window !== 'undefined' && typeof window.document !== 'undefined'")
