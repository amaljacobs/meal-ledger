package com.amaljacobs.mealledger.ui.summary

import com.amaljacobs.mealledger.data.local.FoodEntryEntity
import com.amaljacobs.mealledger.data.local.WaterEntryEntity
import com.amaljacobs.mealledger.data.settings.UserSettings
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklySummaryViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-08-16T12:00:00Z"), ZoneOffset.UTC)
    private val timestamp = Instant.parse("2026-08-15T09:00:00Z")

    @Test
    fun summaryIncludesEmptyDaysAndCalculatesTotals() {
        val summary = summaryForPeriod(
            period = summaryPeriodFor(SummaryMode.Week, LocalDate.parse("2026-08-10")),
            foodEntries = listOf(
                FoodEntryEntity(
                    name = "Lunch",
                    calories = 700,
                    priceMinor = 12550,
                    consumedAt = timestamp,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                ),
            ),
            waterEntries = listOf(
                WaterEntryEntity(1, 1_200, timestamp, timestamp, timestamp),
                WaterEntryEntity(2, 1_300, timestamp, timestamp, timestamp),
            ),
            settings = UserSettings(dailyWaterGoalMl = 2_500),
            clock = clock,
        )

        assertEquals(7, summary.days.size)
        assertEquals(0, summary.days.first().entryCount)
        assertEquals(700, summary.totalCalories)
        assertEquals(12550, summary.totalSpendMinor)
        assertEquals(357, summary.averageWaterMl)
        assertEquals(1, summary.daysAtWaterGoal)
    }

    @Test
    fun weekPeriodStartsOnMondayAndEndsOnSunday() {
        val period = summaryPeriodFor(SummaryMode.Week, LocalDate.parse("2026-08-16"))

        assertEquals(LocalDate.parse("2026-08-10"), period.startDate)
        assertEquals(LocalDate.parse("2026-08-16"), period.endDate)
        assertEquals(7, period.days.size)
    }

    @Test
    fun monthPeriodIncludesEveryDateInTheMonth() {
        val period = summaryPeriodFor(SummaryMode.Month, LocalDate.parse("2026-02-12"))

        assertEquals(LocalDate.parse("2026-02-01"), period.startDate)
        assertEquals(LocalDate.parse("2026-02-28"), period.endDate)
        assertEquals(28, period.days.size)
    }
}
