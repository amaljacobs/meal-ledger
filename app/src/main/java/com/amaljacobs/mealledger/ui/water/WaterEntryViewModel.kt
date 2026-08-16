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
    val loading: Boolean = false,
)

class WaterEntryViewModel(
    private val repository: MealLedgerRepository,
    private val onSaved: () -> Unit,
    entryId: Long? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _state = MutableStateFlow(WaterEntryFormState())
    val state: StateFlow<WaterEntryFormState> = _state.asStateFlow()
    private var existingEntry: WaterEntryEntity? = null

    init {
        if (entryId != null) load(entryId)
    }

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
            val entry = WaterEntryEntity(
                id = existingEntry?.id ?: 0,
                amountMl = amount,
                consumedAt = existingEntry?.consumedAt ?: now,
                createdAt = existingEntry?.createdAt ?: now,
                updatedAt = now,
            )
            if (existingEntry == null) repository.addWaterEntry(entry) else repository.updateWaterEntry(entry)
            onSaved()
        }
    }

    fun delete() {
        val entry = existingEntry ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            repository.deleteWaterEntry(entry)
            onSaved()
        }
    }

    private fun load(entryId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val entry = repository.getWaterEntry(entryId)
            if (entry == null) {
                _state.value = _state.value.copy(loading = false, error = "This water entry no longer exists")
                return@launch
            }
            existingEntry = entry
            _state.value = WaterEntryFormState(amountMl = entry.amountMl.toString())
        }
    }

    companion object {
        fun factory(repository: MealLedgerRepository, onSaved: () -> Unit, entryId: Long? = null) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = WaterEntryViewModel(repository, onSaved, entryId) as T
        }
    }
}
