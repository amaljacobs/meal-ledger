package com.amaljacobs.mealledger.ui.today

import com.amaljacobs.mealledger.data.local.FoodEntryEntity
import com.amaljacobs.mealledger.data.local.WaterEntryEntity
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayViewModelTest {
    private val timestamp = Instant.parse("2026-08-16T09:00:00Z")

    @Test
    fun calculateDailyTotalsExcludesUnknownFoodValues() {
        val foodEntries = listOf(
            FoodEntryEntity(
                name = "Breakfast",
                calories = 450,
                priceMinor = 8000,
                consumedAt = timestamp,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
            FoodEntryEntity(
                name = "Snack",
                consumedAt = timestamp,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        val waterEntries = listOf(
            WaterEntryEntity(amountMl = 250, consumedAt = timestamp, createdAt = timestamp, updatedAt = timestamp),
            WaterEntryEntity(amountMl = 500, consumedAt = timestamp, createdAt = timestamp, updatedAt = timestamp),
        )

        assertEquals(DailyTotals(calories = 450, spendMinor = 8000, waterMl = 750), calculateDailyTotals(foodEntries, waterEntries))
    }

    @Test
    fun timelineEntriesSortByTimeThenIdDescending() {
        val earlierFood = FoodEntryEntity(
            id = 3,
            name = "Breakfast",
            consumedAt = timestamp.minusSeconds(60),
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        val water = WaterEntryEntity(
            id = 2,
            amountMl = 250,
            consumedAt = timestamp,
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        val food = FoodEntryEntity(
            id = 5,
            name = "Lunch",
            consumedAt = timestamp,
            createdAt = timestamp,
            updatedAt = timestamp,
        )

        assertEquals(listOf(5L, 2L, 3L), timelineEntries(listOf(earlierFood, food), listOf(water)).map { it.id })
    }
}
