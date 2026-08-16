package com.amaljacobs.mealledger

import android.app.Application
import com.amaljacobs.mealledger.data.local.MealLedgerDatabase
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository

class MealLedgerApplication : Application() {
    val repository: MealLedgerRepository by lazy {
        val database = MealLedgerDatabase.create(this)
        MealLedgerRepository(database.foodEntryDao(), database.waterEntryDao())
    }
}
