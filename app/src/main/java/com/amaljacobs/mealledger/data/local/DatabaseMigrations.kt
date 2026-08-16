package com.amaljacobs.mealledger.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE food_entries ADD COLUMN protein_grams INTEGER")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS daily_goals (
                    effective_date TEXT NOT NULL,
                    daily_water_goal_ml INTEGER NOT NULL,
                    daily_calorie_goal INTEGER,
                    daily_protein_goal_grams INTEGER,
                    PRIMARY KEY(effective_date)
                )
                """.trimIndent(),
            )
        }
    }

    val all = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
