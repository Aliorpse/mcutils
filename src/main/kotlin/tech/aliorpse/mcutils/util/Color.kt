package tech.aliorpse.mcutils.util

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

    companion object {
        /**
         * Returns a Color based on the input value, can be named or just hex string.
         */
        fun fromString(value: String?): Color? {
            if (value == null) return null
            Named.fromName(value)?.let { return it }

            if (value.matches(Regex("^#[0-9a-fA-F]{6}$"))) {
                return Custom(value.lowercase())
            }

            return null
        }
    }
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