package com.amaljacobs.mealledger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FoodEntryEntity::class, WaterEntryEntity::class, DailyGoalEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(InstantConverters::class)
abstract class MealLedgerDatabase : RoomDatabase() {
    abstract fun foodEntryDao(): FoodEntryDao

    abstract fun waterEntryDao(): WaterEntryDao

    abstract fun dailyGoalDao(): DailyGoalDao

    companion object {
        const val DATABASE_NAME = "meal-ledger.db"

        fun create(context: Context): MealLedgerDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                MealLedgerDatabase::class.java,
                DATABASE_NAME,
            ).addMigrations(*DatabaseMigrations.all)
                .build()
    }
}
