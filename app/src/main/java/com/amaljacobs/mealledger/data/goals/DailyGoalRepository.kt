package com.amaljacobs.mealledger.data.goals

import com.amaljacobs.mealledger.data.local.DailyGoalDao
import com.amaljacobs.mealledger.data.local.DailyGoalEntity
import com.amaljacobs.mealledger.data.settings.UserSettings
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface DailyGoalStore {
    val goals: Flow<List<DailyGoalEntity>>

    suspend fun ensureBaseline(settings: UserSettings)

    suspend fun saveForToday(settings: UserSettings, today: LocalDate)
}

class DailyGoalRepository(private val dailyGoalDao: DailyGoalDao) : DailyGoalStore {
    override val goals: Flow<List<DailyGoalEntity>> = dailyGoalDao.observeAll()

    override suspend fun ensureBaseline(settings: UserSettings) {
        if (dailyGoalDao.count() == 0) {
            dailyGoalDao.upsert(settings.toGoalEntity(BASELINE_EFFECTIVE_DATE))
        }
    }

    override suspend fun saveForToday(settings: UserSettings, today: LocalDate) {
        dailyGoalDao.upsert(settings.toGoalEntity(today.toString()))
    }

    private fun UserSettings.toGoalEntity(effectiveDate: String) = DailyGoalEntity(
        effectiveDate = effectiveDate,
        dailyWaterGoalMl = dailyWaterGoalMl,
        dailyCalorieGoal = dailyCalorieGoal,
        dailyProteinGoalGrams = dailyProteinGoalGrams,
    )

    private companion object {
        const val BASELINE_EFFECTIVE_DATE = "0001-01-01"
    }
}

fun goalForDate(date: LocalDate, goals: List<DailyGoalEntity>, fallback: DailyGoal): DailyGoal =
    goals.lastOrNull { it.effectiveDate <= date.toString() }?.let {
        DailyGoal(it.dailyWaterGoalMl, it.dailyCalorieGoal, it.dailyProteinGoalGrams)
    } ?: fallback

fun UserSettings.toDailyGoal(): DailyGoal = DailyGoal(
    waterMl = dailyWaterGoalMl,
    calories = dailyCalorieGoal,
    proteinGrams = dailyProteinGoalGrams,
)
