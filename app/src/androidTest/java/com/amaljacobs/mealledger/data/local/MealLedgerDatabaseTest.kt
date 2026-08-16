package com.amaljacobs.mealledger.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MealLedgerDatabaseTest {
    private lateinit var database: MealLedgerDatabase
    private lateinit var repository: MealLedgerRepository

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MealLedgerDatabase::class.java).build()
        repository = MealLedgerRepository(database.foodEntryDao(), database.waterEntryDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun foodEntriesArePersistedOrderedAndLimitedToTheRequestedDay() = runBlocking {
        val dayStart = Instant.parse("2026-08-16T00:00:00Z")
        val createdAt = Instant.parse("2026-08-16T08:00:00Z")
        repository.addFoodEntry(
            FoodEntryEntity(
                name = "Breakfast",
                consumedAt = Instant.parse("2026-08-16T08:00:00Z"),
                mealType = MealType.BREAKFAST,
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )
        repository.addFoodEntry(
            FoodEntryEntity(
                name = "Lunch",
                consumedAt = Instant.parse("2026-08-16T13:00:00Z"),
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )
        repository.addFoodEntry(
            FoodEntryEntity(
                name = "Outside the day",
                consumedAt = Instant.parse("2026-08-17T00:00:00Z"),
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )

        val entries = repository.observeFoodEntries(dayStart, Instant.parse("2026-08-17T00:00:00Z")).first()

        assertEquals(listOf("Lunch", "Breakfast"), entries.map(FoodEntryEntity::name))
        assertEquals(MealType.BREAKFAST, entries.last().mealType)
    }

    @Test
    fun waterEntriesArePersistedAndObservable() = runBlocking {
        val occurredAt = Instant.parse("2026-08-16T10:30:00Z")
        val entryId = repository.addWaterEntry(
            WaterEntryEntity(
                amountMl = 500,
                consumedAt = occurredAt,
                createdAt = occurredAt,
                updatedAt = occurredAt,
            ),
        )

        val entries = repository.observeWaterEntries(
            Instant.parse("2026-08-16T00:00:00Z"),
            Instant.parse("2026-08-17T00:00:00Z"),
        ).first()

        assertEquals(entryId, entries.single().id)
        assertEquals(500, entries.single().amountMl)
    }
}
