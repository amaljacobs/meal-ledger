package com.amaljacobs.mealledger.ui.food

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FoodEntryViewModelTest {
    @Test
    fun parsePriceMinorConvertsDecimalAmountsWithoutFloatingPointRounding() {
        assertEquals(999L, parsePriceMinor("9.99"))
        assertEquals(1_000L, parsePriceMinor("10"))
        assertEquals(50L, parsePriceMinor("0.5"))
    }

    @Test
    fun parsePriceMinorRejectsInvalidOrOverPreciseAmounts() {
        assertNull(parsePriceMinor("12.345"))
        assertNull(parsePriceMinor("-1"))
        assertNull(parsePriceMinor("one hundred"))
    }
}
