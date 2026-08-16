package com.amaljacobs.mealledger.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amaljacobs.mealledger.data.local.FoodEntryEntity
import com.amaljacobs.mealledger.data.local.WaterEntryEntity
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class DailyTotals(
    val calories: Int = 0,
    val spendMinor: Long = 0,
    val waterMl: Int = 0,
)

sealed interface TimelineEntry {
    val id: Long
    val consumedAt: Instant

    data class Food(val entry: FoodEntryEntity) : TimelineEntry {
        override val id: Long = entry.id
        override val consumedAt: Instant = entry.consumedAt
    }

    data class Water(val entry: WaterEntryEntity) : TimelineEntry {
        override val id: Long = entry.id
        override val consumedAt: Instant = entry.consumedAt
    }
}

sealed interface TodayUiState {
    data object Loading : TodayUiState

    data class Ready(
        val selectedDate: LocalDate,
        val entries: List<TimelineEntry>,
        val totals: DailyTotals,
    ) : TodayUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val repository: MealLedgerRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now(clock))

    val uiState: StateFlow<TodayUiState> = selectedDate.flatMapLatest { date ->
        val zoneId = clock.zone
        val start = date.atStartOfDay(zoneId).toInstant()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        combine(
            repository.observeFoodEntries(start, end),
            repository.observeWaterEntries(start, end),
        ) { foodEntries, waterEntries ->
            TodayUiState.Ready(
                selectedDate = date,
                entries = timelineEntries(foodEntries, waterEntries),
                totals = calculateDailyTotals(foodEntries, waterEntries),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState.Loading,
    )

    fun showPreviousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun showNextDay() {
        selectedDate.value = selectedDate.value.plusDays(1)
    }

    companion object {
        fun factory(repository: MealLedgerRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TodayViewModel(repository) as T
            }
    }
}

fun calculateDailyTotals(
    foodEntries: List<FoodEntryEntity>,
    waterEntries: List<WaterEntryEntity>,
): DailyTotals = DailyTotals(
    calories = foodEntries.sumOf { it.calories ?: 0 },
    spendMinor = foodEntries.sumOf { it.priceMinor ?: 0L },
    waterMl = waterEntries.sumOf(WaterEntryEntity::amountMl),
)

fun timelineEntries(
    foodEntries: List<FoodEntryEntity>,
    waterEntries: List<WaterEntryEntity>,
): List<TimelineEntry> =
    (foodEntries.map(TimelineEntry::Food) + waterEntries.map(TimelineEntry::Water))
        .sortedWith(compareByDescending<TimelineEntry> { it.consumedAt }.thenByDescending { it.id })
