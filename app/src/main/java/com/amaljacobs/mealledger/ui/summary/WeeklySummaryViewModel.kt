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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class SummaryMode { Week, Month }

data class SummaryPeriod(
    val mode: SummaryMode,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    val days: List<LocalDate> get() = generateSequence(startDate) { date ->
        date.takeIf { it < endDate }?.plusDays(1)
    }.toList()
}

fun summaryPeriodFor(mode: SummaryMode, date: LocalDate): SummaryPeriod = when (mode) {
    SummaryMode.Week -> {
        val startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        SummaryPeriod(mode, startDate, startDate.plusDays(6))
    }
    SummaryMode.Month -> {
        val month = YearMonth.from(date)
        SummaryPeriod(mode, month.atDay(1), month.atEndOfMonth())
    }
}

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
        val period: SummaryPeriod,
        val days: List<WeeklyDaySummary>,
        val totalCalories: Int,
        val totalSpendMinor: Long,
        val averageWaterMl: Int,
        val daysAtWaterGoal: Int,
        val settings: UserSettings,
        val canNavigateForward: Boolean,
    ) : WeeklySummaryUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklySummaryViewModel(
    private val repository: MealLedgerRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val mode = MutableStateFlow(SummaryMode.Week)
    private val selectedDate = MutableStateFlow(LocalDate.now(clock))
    private val period = combine(mode, selectedDate, ::summaryPeriodFor)

    val uiState: StateFlow<WeeklySummaryUiState> = period.flatMapLatest { selectedPeriod ->
        val startInstant = selectedPeriod.startDate.atStartOfDay(clock.zone).toInstant()
        val endInstant = selectedPeriod.endDate.plusDays(1).atStartOfDay(clock.zone).toInstant()
        combine(
            repository.observeFoodEntries(startInstant, endInstant),
            repository.observeWaterEntries(startInstant, endInstant),
            settingsRepository.settings,
        ) { foodEntries, waterEntries, settings ->
            summaryForPeriod(selectedPeriod, foodEntries, waterEntries, settings, clock).copy(
                canNavigateForward = selectedPeriod.startDate < summaryPeriodFor(selectedPeriod.mode, LocalDate.now(clock)).startDate,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeeklySummaryUiState.Loading,
    )

    fun showPreviousPeriod() {
        selectedDate.value = when (mode.value) {
            SummaryMode.Week -> selectedDate.value.minusWeeks(1)
            SummaryMode.Month -> selectedDate.value.minusMonths(1)
        }
    }

    fun showNextPeriod() {
        val nextDate = when (mode.value) {
            SummaryMode.Week -> selectedDate.value.plusWeeks(1)
            SummaryMode.Month -> selectedDate.value.plusMonths(1)
        }
        if (summaryPeriodFor(mode.value, nextDate).startDate <= summaryPeriodFor(mode.value, LocalDate.now(clock)).startDate) {
            selectedDate.value = nextDate
        }
    }

    fun selectMode(newMode: SummaryMode) {
        mode.value = newMode
    }

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

fun summaryForPeriod(
    period: SummaryPeriod,
    foodEntries: List<FoodEntryEntity>,
    waterEntries: List<WaterEntryEntity>,
    settings: UserSettings,
    clock: Clock,
): WeeklySummaryUiState.Ready {
    val zoneId = clock.zone
    val foodByDate = foodEntries.groupBy { it.consumedAt.atZone(zoneId).toLocalDate() }
    val waterByDate = waterEntries.groupBy { it.consumedAt.atZone(zoneId).toLocalDate() }
    val days = period.days.map { date ->
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
        period = period,
        days = days,
        totalCalories = days.sumOf(WeeklyDaySummary::calories),
        totalSpendMinor = days.sumOf(WeeklyDaySummary::spendMinor),
        averageWaterMl = days.sumOf(WeeklyDaySummary::waterMl) / days.size,
        daysAtWaterGoal = days.count { it.waterMl >= settings.dailyWaterGoalMl },
        settings = settings,
        canNavigateForward = false,
    )
}
