package com.amaljacobs.mealledger

import android.app.Application
import com.amaljacobs.mealledger.data.goals.DailyGoalRepository
import com.amaljacobs.mealledger.data.local.MealLedgerDatabase
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import com.amaljacobs.mealledger.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MealLedgerApplication : Application() {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    private val database: MealLedgerDatabase by lazy { MealLedgerDatabase.create(this) }

    val repository: MealLedgerRepository by lazy {
        MealLedgerRepository(database.foodEntryDao(), database.waterEntryDao())
    }

    val dailyGoalRepository: DailyGoalRepository by lazy {
        DailyGoalRepository(database.dailyGoalDao())
    }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            dailyGoalRepository.ensureBaseline(settingsRepository.settings.first())
        }
    }
}
