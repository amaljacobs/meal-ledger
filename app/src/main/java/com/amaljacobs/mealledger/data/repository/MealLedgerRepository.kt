package com.amaljacobs.mealledger.data.repository

import com.amaljacobs.mealledger.data.local.FoodEntryDao
import com.amaljacobs.mealledger.data.local.FoodEntryEntity
import com.amaljacobs.mealledger.data.local.WaterEntryDao
import com.amaljacobs.mealledger.data.local.WaterEntryEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

class MealLedgerRepository(
    private val foodEntryDao: FoodEntryDao,
    private val waterEntryDao: WaterEntryDao,
) {
    fun observeFoodEntries(
        startInclusive: Instant,
        endExclusive: Instant,
    ): Flow<List<FoodEntryEntity>> = foodEntryDao.observeBetween(startInclusive, endExclusive)

    fun observeWaterEntries(
        startInclusive: Instant,
        endExclusive: Instant,
    ): Flow<List<WaterEntryEntity>> = waterEntryDao.observeBetween(startInclusive, endExclusive)

    suspend fun addFoodEntry(entry: FoodEntryEntity): Long = foodEntryDao.insert(entry)

    suspend fun updateFoodEntry(entry: FoodEntryEntity) = foodEntryDao.update(entry)

    suspend fun deleteFoodEntry(entry: FoodEntryEntity) = foodEntryDao.delete(entry)

    suspend fun addWaterEntry(entry: WaterEntryEntity): Long = waterEntryDao.insert(entry)

    suspend fun updateWaterEntry(entry: WaterEntryEntity) = waterEntryDao.update(entry)

    suspend fun deleteWaterEntry(entry: WaterEntryEntity) = waterEntryDao.delete(entry)
}
