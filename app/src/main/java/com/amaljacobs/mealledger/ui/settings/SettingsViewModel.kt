package com.amaljacobs.mealledger.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amaljacobs.mealledger.data.settings.SettingsRepository
import com.amaljacobs.mealledger.data.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currencyCode: String = "INR",
    val dailyWaterGoalMl: String = "2500",
    val cupSizeMl: String = "250",
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.value = SettingsUiState(
                    currencyCode = settings.currencyCode,
                    dailyWaterGoalMl = settings.dailyWaterGoalMl.toString(),
                    cupSizeMl = settings.cupSizeMl.toString(),
                    loading = false,
                )
            }
        }
    }

    fun update(transform: (SettingsUiState) -> SettingsUiState) {
        _state.value = transform(_state.value).copy(error = null)
    }

    fun save() {
        val current = _state.value
        val waterGoal = current.dailyWaterGoalMl.toIntOrNull()
        val cupSize = current.cupSizeMl.toIntOrNull()
        val error = when {
            waterGoal == null || waterGoal !in 1..20_000 -> "Water goal must be from 1 to 20,000 ml"
            cupSize == null || cupSize !in 1..2_000 -> "Cup size must be from 1 to 2,000 ml"
            else -> null
        }
        if (error != null) {
            _state.value = current.copy(error = error)
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(saving = true)
            val validWaterGoal = requireNotNull(waterGoal)
            val validCupSize = requireNotNull(cupSize)
            settingsRepository.update(
                UserSettings(
                    currencyCode = current.currencyCode,
                    dailyWaterGoalMl = validWaterGoal,
                    cupSizeMl = validCupSize,
                ),
            )
        }
    }

    companion object {
        fun factory(settingsRepository: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SettingsViewModel(settingsRepository) as T
            }
    }
}
