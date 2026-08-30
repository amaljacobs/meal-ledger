package com.amaljacobs.mealledger.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MealLedgerDatabaseTest {
    private val migrationDatabaseName = "meal-ledger-migration-test"
    private val persistenceDatabaseName = "meal-ledger-persistence-test"
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
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(migrationDatabaseName)
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(persistenceDatabaseName)
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
                proteinGrams = 24,
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
        assertEquals(24, entries.last().proteinGrams)
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

    @Test
    fun entriesAndGoalHistorySurviveDatabaseRecreation() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val consumedAt = Instant.parse("2026-08-16T10:30:00Z")
        val firstDatabase = Room.databaseBuilder(
            context,
            MealLedgerDatabase::class.java,
            persistenceDatabaseName,
        ).build()
        val firstRepository = MealLedgerRepository(firstDatabase.foodEntryDao(), firstDatabase.waterEntryDao())

        firstRepository.addFoodEntry(
            FoodEntryEntity(
                name = "Overnight oats",
                consumedAt = consumedAt,
                calories = 480,
                createdAt = consumedAt,
                updatedAt = consumedAt,
            ),
        )
        firstRepository.addWaterEntry(
            WaterEntryEntity(
                amountMl = 400,
                consumedAt = consumedAt,
                createdAt = consumedAt,
                updatedAt = consumedAt,
            ),
        )
        firstDatabase.dailyGoalDao().upsert(DailyGoalEntity("2026-08-16", 2_750, 2_100, 120))
        firstDatabase.close()

        val reopenedDatabase = Room.databaseBuilder(
            context,
            MealLedgerDatabase::class.java,
            persistenceDatabaseName,
        ).build()
        val reopenedRepository = MealLedgerRepository(reopenedDatabase.foodEntryDao(), reopenedDatabase.waterEntryDao())

        assertEquals(
            "Overnight oats",
            reopenedRepository.observeFoodEntries(consumedAt.minusSeconds(1), consumedAt.plusSeconds(1)).first().single().name,
        )
        assertEquals(
            400,
            reopenedRepository.observeWaterEntries(consumedAt.minusSeconds(1), consumedAt.plusSeconds(1)).first().single().amountMl,
        )
        assertEquals(
            DailyGoalEntity("2026-08-16", 2_750, 2_100, 120),
            reopenedDatabase.dailyGoalDao().observeAll().first().single(),
        )
        reopenedDatabase.close()
    }

    @Test
    fun dayQueriesRespectLocalDayBoundariesWithoutDuplicates() = runBlocking {
        val zone = ZoneId.of("Asia/Kolkata")
        val day = LocalDate.of(2026, 8, 16)
        val start = day.atStartOfDay(zone).toInstant()
        val end = day.plusDays(1).atStartOfDay(zone).toInstant()
        val timestamp = Instant.parse("2026-08-15T20:00:00Z")
        listOf(start, end.minusMillis(1), end).forEachIndexed { index, consumedAt ->
            repository.addFoodEntry(
                FoodEntryEntity(
                    name = "Food $index",
                    consumedAt = consumedAt,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                ),
            )
            repository.addWaterEntry(
                WaterEntryEntity(
                    amountMl = 100 + index,
                    consumedAt = consumedAt,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                ),
            )
        }

        assertEquals(
            listOf("Food 1", "Food 0"),
            repository.observeFoodEntries(start, end).first().map(FoodEntryEntity::name),
        )
        assertEquals(
            listOf(101, 100),
            repository.observeWaterEntries(start, end).first().map(WaterEntryEntity::amountMl),
        )
    }

    @Test
    fun entriesCanBeUpdatedAndDeleted() = runBlocking {
        val occurredAt = Instant.parse("2026-08-16T10:30:00Z")
        val foodId = repository.addFoodEntry(
            FoodEntryEntity(
                name = "Toast",
                consumedAt = occurredAt,
                createdAt = occurredAt,
                updatedAt = occurredAt,
            ),
        )
        val waterId = repository.addWaterEntry(
            WaterEntryEntity(
                amountMl = 250,
                consumedAt = occurredAt,
                createdAt = occurredAt,
                updatedAt = occurredAt,
            ),
        )

        val food = requireNotNull(repository.getFoodEntry(foodId))
        val water = requireNotNull(repository.getWaterEntry(waterId))
        repository.updateFoodEntry(food.copy(name = "Avocado toast"))
        repository.updateWaterEntry(water.copy(amountMl = 500))

        assertEquals("Avocado toast", repository.getFoodEntry(foodId)?.name)
        assertEquals(500, repository.getWaterEntry(waterId)?.amountMl)

        repository.deleteFoodEntry(requireNotNull(repository.getFoodEntry(foodId)))
        repository.deleteWaterEntry(requireNotNull(repository.getWaterEntry(waterId)))

        assertEquals(null, repository.getFoodEntry(foodId))
        assertEquals(null, repository.getWaterEntry(waterId))
    }

    @Test
    fun migrationFromVersion1PreservesExistingFoodEntries() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val version1Database = context.openOrCreateDatabase(
            migrationDatabaseName,
            Context.MODE_PRIVATE,
            null,
        )
        version1Database.execSQL(
            """
            CREATE TABLE food_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                consumed_at INTEGER NOT NULL,
                meal_type TEXT,
                portion_note TEXT,
                calories INTEGER,
                price_minor INTEGER,
                currency_code TEXT,
                note TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        version1Database.execSQL(
            """
            CREATE TABLE water_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                amount_ml INTEGER NOT NULL,
                consumed_at INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        version1Database.execSQL(
            "CREATE INDEX index_food_entries_consumed_at ON food_entries (consumed_at)",
        )
        version1Database.execSQL(
            "CREATE INDEX index_water_entries_consumed_at ON water_entries (consumed_at)",
        )
        version1Database.execSQL(
            """
            INSERT INTO food_entries (
                id, name, consumed_at, meal_type, portion_note, calories,
                price_minor, currency_code, note, created_at, updated_at
            ) VALUES (
                1, 'Lunch', 1786942800000, 'LUNCH', '1 bowl', 650,
                1299, 'INR', 'With colleagues', 1786924800000, 1786924800000
            )
            """.trimIndent(),
        )
        version1Database.version = 1
        version1Database.close()

        val migratedDatabase = Room.databaseBuilder(
            context,
            MealLedgerDatabase::class.java,
            migrationDatabaseName,
        ).addMigrations(*DatabaseMigrations.all)
            .build()

        migratedDatabase.openHelper.writableDatabase.query(
            "SELECT name, meal_type, calories, price_minor, protein_grams FROM food_entries WHERE id = 1",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Lunch", cursor.getString(0))
            assertEquals("LUNCH", cursor.getString(1))
            assertEquals(650, cursor.getInt(2))
            assertEquals(1299, cursor.getLong(3))
            assertTrue(cursor.isNull(4))
        }
        migratedDatabase.close()
    }

    @Test
    fun migrationFromVersion2CreatesDailyGoalsWithoutChangingEntries() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val version2Database = context.openOrCreateDatabase(migrationDatabaseName, Context.MODE_PRIVATE, null)
        version2Database.execSQL("CREATE TABLE food_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, consumed_at INTEGER NOT NULL, meal_type TEXT, portion_note TEXT, calories INTEGER, protein_grams INTEGER, price_minor INTEGER, currency_code TEXT, note TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        version2Database.execSQL("CREATE TABLE water_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, amount_ml INTEGER NOT NULL, consumed_at INTEGER NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        version2Database.execSQL("CREATE INDEX index_food_entries_consumed_at ON food_entries (consumed_at)")
        version2Database.execSQL("CREATE INDEX index_water_entries_consumed_at ON water_entries (consumed_at)")
        version2Database.execSQL("INSERT INTO water_entries (id, amount_ml, consumed_at, created_at, updated_at) VALUES (1, 500, 1786942800000, 1786942800000, 1786942800000)")
        version2Database.version = 2
        version2Database.close()

        val migratedDatabase = Room.databaseBuilder(context, MealLedgerDatabase::class.java, migrationDatabaseName)
            .addMigrations(*DatabaseMigrations.all)
            .build()

        migratedDatabase.openHelper.writableDatabase.query("SELECT amount_ml FROM water_entries WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(500, cursor.getInt(0))
        }
        migratedDatabase.openHelper.writableDatabase.query("SELECT COUNT(*) FROM daily_goals").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        migratedDatabase.close()
    }
}
