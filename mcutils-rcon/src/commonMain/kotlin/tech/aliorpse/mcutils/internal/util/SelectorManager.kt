package tech.aliorpse.mcutils.internal.util

import io.ktor.network.selector.SelectorManager

internal val globalSelectorIO: SelectorManager = SelectorManager(DispatchersIO)
