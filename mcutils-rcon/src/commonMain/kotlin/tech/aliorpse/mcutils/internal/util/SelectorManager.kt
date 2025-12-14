package tech.aliorpse.mcutils.internal.util

import io.ktor.network.selector.SelectorManager

public val globalSelectorIO: SelectorManager = SelectorManager(DispatchersIO)
