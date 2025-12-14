package tech.aliorpse.mcutils.util

import tech.aliorpse.mcutils.entity.Sample
import tech.aliorpse.mcutils.entity.TextComponent

/**
 * Converts a list of Sample entries into a list of [TextComponent].
 *
 * Useful when player names contain formatting codes.
 */
public fun List<Sample>.toTextComponents(): List<TextComponent> {
    return map { it.name.toTextComponent() }
}
