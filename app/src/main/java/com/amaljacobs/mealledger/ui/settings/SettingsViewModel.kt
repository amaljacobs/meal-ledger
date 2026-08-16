package com.amaljacobs.mealledger.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amaljacobs.mealledger.data.settings.SettingsStore
import com.amaljacobs.mealledger.data.settings.UserSettings
import com.amaljacobs.mealledger.data.goals.DailyGoalStore
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currencyCode: String = "INR",
    val dailyWaterGoalMl: String = "2500",
    val dailyCalorieGoal: String = "",
    val dailyProteinGoalGrams: String = "",
    val cupSizeMl: String = "250",
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsStore,
    private val dailyGoalStore: DailyGoalStore,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                val current = _state.value
                _state.value = if (current.loading) {
                    SettingsUiState(
                        currencyCode = settings.currencyCode,
                        dailyWaterGoalMl = settings.dailyWaterGoalMl.toString(),
                        dailyCalorieGoal = settings.dailyCalorieGoal?.toString().orEmpty(),
                        dailyProteinGoalGrams = settings.dailyProteinGoalGrams?.toString().orEmpty(),
                        cupSizeMl = settings.cupSizeMl.toString(),
                        loading = false,
                    )
                } else {
                    current.copy(loading = false, saving = false)
                }
            }
        }
    }

    fun update(transform: (SettingsUiState) -> SettingsUiState) {
        _state.value = transform(_state.value).copy(error = null)
    }

    fun save() {
        val current = _state.value
        if (current.loading || current.saving) return
        val waterGoal = current.dailyWaterGoalMl.toIntOrNull()
        val calorieGoal = current.dailyCalorieGoal.toIntOrNull()
        val proteinGoal = current.dailyProteinGoalGrams.toIntOrNull()
        val cupSize = current.cupSizeMl.toIntOrNull()
        val error = when {
            waterGoal == null || waterGoal !in 1..20_000 -> "Water goal must be from 1 to 20,000 ml"
            current.dailyCalorieGoal.isNotBlank() && calorieGoal !in 1..10_000 -> "Calorie goal must be from 1 to 10,000 kcal"
            current.dailyProteinGoalGrams.isNotBlank() && proteinGoal !in 1..500 -> "Protein goal must be from 1 to 500 g"
            cupSize == null || cupSize !in 1..2_000 -> "Cup size must be from 1 to 2,000 ml"
            else -> null
        }
        if (error != null) {
            _state.value = current.copy(error = error)
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(saving = true)
            try {
                settingsRepository.update(
                    UserSettings(
                        currencyCode = current.currencyCode,
                        dailyWaterGoalMl = requireNotNull(waterGoal),
                        dailyCalorieGoal = calorieGoal,
                        dailyProteinGoalGrams = proteinGoal,
                        cupSizeMl = requireNotNull(cupSize),
                    ),
                )
                dailyGoalStore.saveForToday(
                    UserSettings(
                        currencyCode = current.currencyCode,
                        dailyWaterGoalMl = requireNotNull(waterGoal),
                        dailyCalorieGoal = calorieGoal,
                        dailyProteinGoalGrams = proteinGoal,
                        cupSizeMl = requireNotNull(cupSize),
                    ),
                    LocalDate.now(clock),
                )
                _state.value = _state.value.copy(saving = false)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _state.value = _state.value.copy(
                    saving = false,
                    error = "Could not save settings. Please try again.",
                )
            }
        }
    }

    companion object {
        fun factory(settingsRepository: SettingsStore, dailyGoalStore: DailyGoalStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SettingsViewModel(settingsRepository, dailyGoalStore) as T
            }
    }
}
