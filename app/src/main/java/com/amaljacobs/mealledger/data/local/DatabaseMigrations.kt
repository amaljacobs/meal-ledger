package com.amaljacobs.mealledger.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE food_entries ADD COLUMN protein_grams INTEGER")
        }
    }

    val all = arrayOf(MIGRATION_1_2)
}
