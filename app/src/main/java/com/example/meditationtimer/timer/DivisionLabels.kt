package com.example.meditationtimer.timer

/** The offered ways to divide a sit: gong at each half, third, quarter, fifth, eighth. */
val DivisionOptions = listOf(2, 3, 4, 5, 8)

private val Glyphs = mapOf(
    (1 to 2) to "½",
    (1 to 3) to "⅓", (2 to 3) to "⅔",
    (1 to 4) to "¼", (3 to 4) to "¾",
    (1 to 5) to "⅕", (2 to 5) to "⅖", (3 to 5) to "⅗", (4 to 5) to "⅘",
    (1 to 8) to "⅛", (3 to 8) to "⅜", (5 to 8) to "⅝", (7 to 8) to "⅞"
)

/** Glyph for one part of a division, for the picker pills: 4 -> "¼". */
fun divisionGlyph(divisions: Int): String = Glyphs[1 to divisions] ?: "1/$divisions"

/**
 * Dial labels for the division marks, clockwise; the last one is the full
 * duration at the top. E.g. 10 minutes in thirds -> ["3⅓", "6⅔", "10"].
 */
fun divisionLabels(durationMinutes: Int, divisions: Int): List<String> =
    (1..divisions).map { k -> mixedNumber(k * durationMinutes, divisions) }

/** "3⅓"-style mixed number for minutesNumerator/denominator minutes. */
internal fun mixedNumber(minutesNumerator: Int, denominator: Int): String {
    val whole = minutesNumerator / denominator
    val rem = minutesNumerator % denominator
    if (rem == 0) return "$whole"
    val g = gcd(rem, denominator)
    val glyph = Glyphs[(rem / g) to (denominator / g)] ?: "${rem / g}/${denominator / g}"
    return if (whole == 0) glyph else "$whole$glyph"
}

private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
