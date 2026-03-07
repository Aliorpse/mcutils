package tech.aliorpse.mcutils.internal.util

import io.ktor.network.selector.*

internal val globalSelectorIO: SelectorManager = SelectorManager(DispatchersIO)
