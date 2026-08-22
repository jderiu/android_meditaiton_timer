package com.example.meditationtimer

import com.example.meditationtimer.timer.divisionGlyph
import com.example.meditationtimer.timer.divisionLabels
import org.junit.Assert.assertEquals
import org.junit.Test

class DivisionLabelsTest {

    @Test
    fun tenMinutesInQuarters() {
        assertEquals(listOf("2½", "5", "7½", "10"), divisionLabels(10, 4))
    }

    @Test
    fun tenMinutesInThirds() {
        assertEquals(listOf("3⅓", "6⅔", "10"), divisionLabels(10, 3))
    }

    @Test
    fun tenMinutesInEighths() {
        assertEquals(listOf("1¼", "2½", "3¾", "5", "6¼", "7½", "8¾", "10"), divisionLabels(10, 8))
    }

    @Test
    fun sevenMinutesInFifths() {
        assertEquals(listOf("1⅖", "2⅘", "4⅕", "5⅗", "7"), divisionLabels(7, 5))
    }

    @Test
    fun oneMinuteInHalves() {
        assertEquals(listOf("½", "1"), divisionLabels(1, 2))
    }

    @Test
    fun glyphs() {
        assertEquals("½", divisionGlyph(2))
        assertEquals("⅛", divisionGlyph(8))
    }
}
