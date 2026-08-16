package com.amaljacobs.mealledger.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amaljacobs.mealledger.data.local.FoodEntryEntity
import com.amaljacobs.mealledger.data.local.WaterEntryEntity
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import com.amaljacobs.mealledger.data.settings.SettingsRepository
import com.amaljacobs.mealledger.data.settings.UserSettings
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class WeeklyDaySummary(
    val date: LocalDate,
    val calories: Int = 0,
    val spendMinor: Long = 0,
    val waterMl: Int = 0,
    val entryCount: Int = 0,
)

sealed interface WeeklySummaryUiState {
    data object Loading : WeeklySummaryUiState

    data class Ready(
        val days: List<WeeklyDaySummary>,
        val totalCalories: Int,
        val totalSpendMinor: Long,
        val averageWaterMl: Int,
        val daysAtWaterGoal: Int,
        val settings: UserSettings,
    ) : WeeklySummaryUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklySummaryViewModel(
    private val repository: MealLedgerRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val endDate = LocalDate.now(clock)
    private val startDate = endDate.minusDays(6)
    private val startInstant = startDate.atStartOfDay(clock.zone).toInstant()
    private val endInstant = endDate.plusDays(1).atStartOfDay(clock.zone).toInstant()

    val uiState: StateFlow<WeeklySummaryUiState> = combine(
        repository.observeFoodEntries(startInstant, endInstant),
        repository.observeWaterEntries(startInstant, endInstant),
        settingsRepository.settings,
    ) { foodEntries, waterEntries, settings ->
        weeklySummary(
            startDate = startDate,
            foodEntries = foodEntries,
            waterEntries = waterEntries,
            settings = settings,
            clock = clock,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeeklySummaryUiState.Loading,
    )

    companion object {
        fun factory(
            repository: MealLedgerRepository,
            settingsRepository: SettingsRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    WeeklySummaryViewModel(repository, settingsRepository) as T
            }
    }
}

fun weeklySummary(
    startDate: LocalDate,
    foodEntries: List<FoodEntryEntity>,
    waterEntries: List<WaterEntryEntity>,
    settings: UserSettings,
    clock: Clock,
): WeeklySummaryUiState.Ready {
    val zoneId = clock.zone
    val foodByDate = foodEntries.groupBy { it.consumedAt.atZone(zoneId).toLocalDate() }
    val waterByDate = waterEntries.groupBy { it.consumedAt.atZone(zoneId).toLocalDate() }
    val days = (0L..6L).map { offset ->
        val date = startDate.plusDays(offset)
        val foodForDay = foodByDate[date].orEmpty()
        val waterForDay = waterByDate[date].orEmpty()
        WeeklyDaySummary(
            date = date,
            calories = foodForDay.sumOf { it.calories ?: 0 },
            spendMinor = foodForDay.sumOf { it.priceMinor ?: 0L },
            waterMl = waterForDay.sumOf(WaterEntryEntity::amountMl),
            entryCount = foodForDay.size + waterForDay.size,
        )
    }

    return WeeklySummaryUiState.Ready(
        days = days,
        totalCalories = days.sumOf(WeeklyDaySummary::calories),
        totalSpendMinor = days.sumOf(WeeklyDaySummary::spendMinor),
        averageWaterMl = days.sumOf(WeeklyDaySummary::waterMl) / days.size,
        daysAtWaterGoal = days.count { it.waterMl >= settings.dailyWaterGoalMl },
        settings = settings,
    )
}
