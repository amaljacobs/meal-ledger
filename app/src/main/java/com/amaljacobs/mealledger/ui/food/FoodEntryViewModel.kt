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
)

class FoodEntryViewModel(
    private val repository: MealLedgerRepository,
    private val onSaved: () -> Unit,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _state = MutableStateFlow(FoodEntryFormState())
    val state: StateFlow<FoodEntryFormState> = _state.asStateFlow()

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
            repository.addFoodEntry(FoodEntryEntity(name = current.name.trim(), consumedAt = now, mealType = current.mealType, portionNote = current.portionNote.trim().ifBlank { null }, calories = calories, proteinGrams = protein, priceMinor = price?.let { (it * 100).toLong() }, currencyCode = if (price == null) null else "INR", note = current.note.trim().ifBlank { null }, createdAt = now, updatedAt = now))
            onSaved()
        }
    }

    companion object {
        fun factory(repository: MealLedgerRepository, onSaved: () -> Unit) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = FoodEntryViewModel(repository, onSaved) as T
        }
    }
}
