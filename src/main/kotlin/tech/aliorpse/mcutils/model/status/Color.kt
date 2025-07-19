package tech.aliorpse.mcutils.model.status

/**
 * Represents a color in Minecraft.
 *
 * @property hex The hexadecimal value representing the color.
 */
sealed interface Color {
    val hex: String

    /**
     * Colors in Minecraft that "have a name".
     */
    enum class Named(override val hex: String) : Color {
        BLACK("#000000"),
        DARK_BLUE("#0000AA"),
        DARK_GREEN("#00AA00"),
        DARK_AQUA("#00AAAA"),
        DARK_RED("#AA0000"),
        DARK_PURPLE("#AA00AA"),
        GOLD("#FFAA00"),
        GRAY("#AAAAAA"),
        DARK_GRAY("#555555"),
        BLUE("#5555FF"),
        GREEN("#55FF55"),
        AQUA("#55FFFF"),
        RED("#FF5555"),
        LIGHT_PURPLE("#FF55FF"),
        YELLOW("#FFFF55"),
        WHITE("#FFFFFF");

        companion object {
            private val codeToColor = mapOf(
                '0' to BLACK,
                '1' to DARK_BLUE,
                '2' to DARK_GREEN,
                '3' to DARK_AQUA,
                '4' to DARK_RED,
                '5' to DARK_PURPLE,
                '6' to GOLD,
                '7' to GRAY,
                '8' to DARK_GRAY,
                '9' to BLUE,
                'a' to GREEN,
                'b' to AQUA,
                'c' to RED,
                'd' to LIGHT_PURPLE,
                'e' to YELLOW,
                'f' to WHITE
            )

            /**
             * Get a [Color.Named] from Minecraft format code (e.g. 'a', 'c').
             */
            fun fromCode(code: Char): Named? = codeToColor[code.lowercaseChar()]

            private val NAME_MAP = entries.associateBy { it.name.lowercase() }

            /**
             * Return a Color based on the inputted name
             */
            fun fromName(name: String): Named? = NAME_MAP[name.lowercase()]
        }
    }

    /**
     * Custom colors using hex strings.
     */
    data class Custom(override val hex: String) : Color
}

/**
 * Map from name to code in Minecraft.
 */
val colorToOriginalCode = mapOf(
    Color.Named.BLACK to '0',
    Color.Named.DARK_BLUE to '1',
    Color.Named.DARK_GREEN to '2',
    Color.Named.DARK_AQUA to '3',
    Color.Named.DARK_RED to '4',
    Color.Named.DARK_PURPLE to '5',
    Color.Named.GOLD to '6',
    Color.Named.GRAY to '7',
    Color.Named.DARK_GRAY to '8',
    Color.Named.BLUE to '9',
    Color.Named.GREEN to 'a',
    Color.Named.AQUA to 'b',
    Color.Named.RED to 'c',
    Color.Named.LIGHT_PURPLE to 'd',
    Color.Named.YELLOW to 'e',
    Color.Named.WHITE to 'f',
)
