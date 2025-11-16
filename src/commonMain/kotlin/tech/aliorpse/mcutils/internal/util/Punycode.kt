package tech.aliorpse.mcutils.internal.util

@Suppress("MagicNumber")
internal object Punycode {
    private const val BASE = 36
    private const val T_MIN = 1
    private const val T_MAX = 26
    private const val SKEW = 38
    private const val DAMP = 700
    private const val INITIAL_BIAS = 72
    private const val INITIAL_N = 128
    private const val DELIMITER = '-'

    internal fun from(domain: String): String =
        domain.split('.').joinToString(".") { label ->
            if (label.any { it.code > 127 }) "xn--" + encodeLabel(label) else label
        }

    private fun encodeLabel(input: String): String {
        val output = StringBuilder()
        var n = INITIAL_N
        var delta = 0L
        var bias = INITIAL_BIAS

        val codePoints = input.toCodePoints()

        // copy basic ASCII
        for (cp in codePoints) if (cp < 0x80) output.append(cp.toChar())
        val basicLength = output.length
        var handled = basicLength
        if (basicLength > 0) output.append(DELIMITER)

        val total = codePoints.size
        while (handled < total) {
            var m = Int.MAX_VALUE
            for (cp in codePoints) if (cp in n..<m) m = cp
            delta += (m - n).toLong() * (handled + 1)
            n = m

            for (cp in codePoints) {
                if (cp < n) delta++
                if (cp == n) {
                    var q = delta
                    var k = BASE
                    while (true) {
                        val t = when {
                            k <= bias -> T_MIN
                            k >= bias + T_MAX -> T_MAX
                            else -> k - bias
                        }
                        if (q < t) break
                        val digit = (t + ((q - t) % (BASE - t))).toInt()
                        output.append(encodeDigit(digit))
                        q = (q - t) / (BASE - t)
                        k += BASE
                    }
                    output.append(encodeDigit(q.toInt()))
                    bias = adapt(delta, handled + 1, handled == basicLength)
                    delta = 0L
                    handled++
                }
            }
            delta++
            n++
        }

        return output.toString()
    }

    private fun adapt(delta: Long, numPoints: Int, firstTime: Boolean): Int {
        var d = if (firstTime) delta / DAMP else delta / 2
        d += d / numPoints
        var k = 0
        val limit = (BASE - T_MIN) * T_MAX / 2
        while (d > limit) {
            d /= BASE - T_MIN
            k += BASE
        }
        return k + ((BASE - T_MIN + 1) * d / (d + SKEW)).toInt()
    }

    private fun encodeDigit(d: Int): Char =
        if (d < 26) ('a'.code + d).toChar() else ('0'.code + (d - 26)).toChar()

    private fun String.toCodePoints(): List<Int> {
        val list = mutableListOf<Int>()
        var i = 0
        while (i < length) {
            val c1 = this[i]
            if (c1 in '\uD800'..'\uDBFF' && i + 1 < length) {
                val c2 = this[i + 1]
                if (c2 in '\uDC00'..'\uDFFF') {
                    val high = c1.code
                    val low = c2.code
                    val codePoint = (high - 0xD800 shl 10) + (low - 0xDC00) + 0x10000
                    list.add(codePoint)
                    i += 2
                    continue
                }
            }
            list.add(c1.code)
            i++
        }
        return list
    }
}
