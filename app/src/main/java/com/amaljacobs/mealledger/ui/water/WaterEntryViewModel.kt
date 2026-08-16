package com.amaljacobs.mealledger.ui.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amaljacobs.mealledger.data.local.WaterEntryEntity
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import java.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WaterEntryFormState(
    val amountMl: String = "",
    val error: String? = null,
    val saving: Boolean = false,
)

class WaterEntryViewModel(
    private val repository: MealLedgerRepository,
    private val onSaved: () -> Unit,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _state = MutableStateFlow(WaterEntryFormState())
    val state: StateFlow<WaterEntryFormState> = _state.asStateFlow()

    fun setAmount(amountMl: String) {
        _state.value = _state.value.copy(amountMl = amountMl, error = null)
    }

    fun save() {
        val current = _state.value
        val amount = current.amountMl.toIntOrNull()
        if (amount == null || amount !in 1..10_000) {
            _state.value = current.copy(error = "Enter an amount from 1 to 10,000 ml")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(saving = true)
            val now = clock.instant()
            repository.addWaterEntry(WaterEntryEntity(amountMl = amount, consumedAt = now, createdAt = now, updatedAt = now))
            onSaved()
        }
    }

    companion object {
        fun factory(repository: MealLedgerRepository, onSaved: () -> Unit) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = WaterEntryViewModel(repository, onSaved) as T
        }
    }
}
