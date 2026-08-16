package com.amaljacobs.mealledger.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amaljacobs.mealledger.data.local.FoodEntryEntity
import com.amaljacobs.mealledger.data.local.MealType
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import java.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FoodEntryFormState(
    val name: String = "",
    val portionNote: String = "",
    val calories: String = "",
    val proteinGrams: String = "",
    val price: String = "",
    val note: String = "",
    val mealType: MealType? = null,
    val error: String? = null,
    val saving: Boolean = false,
    val loading: Boolean = false,
)

class FoodEntryViewModel(
    private val repository: MealLedgerRepository,
    private val onSaved: () -> Unit,
    entryId: Long? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _state = MutableStateFlow(FoodEntryFormState())
    val state: StateFlow<FoodEntryFormState> = _state.asStateFlow()
    private var existingEntry: FoodEntryEntity? = null

    init {
        if (entryId != null) load(entryId)
    }

    fun update(transform: (FoodEntryFormState) -> FoodEntryFormState) { _state.value = transform(_state.value).copy(error = null) }

    fun save() {
        val current = _state.value
        val calories = current.calories.toIntOrNull()
        val protein = current.proteinGrams.toIntOrNull()
        val price = current.price.toDoubleOrNull()
        val error = when {
            current.name.trim().isEmpty() -> "Food name is required"
            current.name.trim().length > 100 -> "Food name must be 100 characters or fewer"
            current.calories.isNotBlank() && (calories == null || calories < 0) -> "Calories must be a non-negative whole number"
            current.proteinGrams.isNotBlank() && (protein == null || protein < 0) -> "Protein must be a non-negative whole number"
            current.price.isNotBlank() && (price == null || price < 0) -> "Price must be a non-negative number"
            else -> null
        }
        if (error != null) { _state.value = current.copy(error = error); return }
        viewModelScope.launch {
            _state.value = current.copy(saving = true)
            val now = clock.instant()
            val entry = FoodEntryEntity(
                id = existingEntry?.id ?: 0,
                name = current.name.trim(),
                consumedAt = existingEntry?.consumedAt ?: now,
                mealType = current.mealType,
                portionNote = current.portionNote.trim().ifBlank { null },
                calories = calories,
                proteinGrams = protein,
                priceMinor = price?.let { (it * 100).toLong() },
                currencyCode = if (price == null) null else "INR",
                note = current.note.trim().ifBlank { null },
                createdAt = existingEntry?.createdAt ?: now,
                updatedAt = now,
            )
            if (existingEntry == null) repository.addFoodEntry(entry) else repository.updateFoodEntry(entry)
            onSaved()
        }
    }

    fun delete() {
        val entry = existingEntry ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            repository.deleteFoodEntry(entry)
            onSaved()
        }
    }

    private fun load(entryId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val entry = repository.getFoodEntry(entryId)
            if (entry == null) {
                _state.value = _state.value.copy(loading = false, error = "This food entry no longer exists")
                return@launch
            }
            existingEntry = entry
            _state.value = FoodEntryFormState(
                name = entry.name,
                portionNote = entry.portionNote.orEmpty(),
                calories = entry.calories?.toString().orEmpty(),
                proteinGrams = entry.proteinGrams?.toString().orEmpty(),
                price = entry.priceMinor?.let { "%.2f".format(it / 100.0) }.orEmpty(),
                note = entry.note.orEmpty(),
                mealType = entry.mealType,
            )
        }
    }

    companion object {
        fun factory(repository: MealLedgerRepository, onSaved: () -> Unit, entryId: Long? = null) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = FoodEntryViewModel(repository, onSaved, entryId) as T
        }
    }
}
