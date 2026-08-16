package com.amaljacobs.mealledger

import android.app.Application
import com.amaljacobs.mealledger.data.local.MealLedgerDatabase
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import com.amaljacobs.mealledger.data.settings.SettingsRepository

class MealLedgerApplication : Application() {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    val repository: MealLedgerRepository by lazy {
        val database = MealLedgerDatabase.create(this)
        MealLedgerRepository(database.foodEntryDao(), database.waterEntryDao())
    }
}
