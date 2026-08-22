package com.example.meditationtimer.timer

import android.content.Context

/**
 * Shuffle-bag over [Koans]: every koan is dealt once before any repeats,
 * and the first card of a fresh bag is never the last card of the old one.
 * Position survives restarts via SharedPreferences.
 */
object KoanDeck {

    private const val PREFS = "koan_deck"
    private const val KEY_ORDER = "order"
    private const val KEY_POS = "pos"

    fun next(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var order = prefs.getString(KEY_ORDER, null)
            ?.split(',')
            ?.mapNotNull { it.toIntOrNull() }
        var pos = prefs.getInt(KEY_POS, 0)

        if (order == null || order.size != Koans.size || order.any { it !in Koans.indices } || pos >= order.size) {
            val lastDealt = order?.lastOrNull() ?: -1
            val shuffled = Koans.indices.shuffled().toMutableList()
            if (shuffled.size > 1 && shuffled.first() == lastDealt) {
                val swapWith = 1 + (0 until shuffled.size - 1).random()
                val tmp = shuffled[0]
                shuffled[0] = shuffled[swapWith]
                shuffled[swapWith] = tmp
            }
            order = shuffled
            pos = 0
            prefs.edit().putString(KEY_ORDER, shuffled.joinToString(",")).putInt(KEY_POS, 0).apply()
        }

        val koan = Koans[order[pos]]
        prefs.edit().putInt(KEY_POS, pos + 1).apply()
        return koan
    }
}
