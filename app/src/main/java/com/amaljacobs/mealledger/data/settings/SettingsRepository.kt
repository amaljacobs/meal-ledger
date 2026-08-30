package com.amaljacobs.mealledger.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.Currency
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SETTINGS_FILE_NAME = "user_settings"
private val Context.settingsDataStore by preferencesDataStore(name = SETTINGS_FILE_NAME)

interface SettingsStore {
    val settings: Flow<UserSettings>

    suspend fun update(settings: UserSettings)
}

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val currencyCodeProvider: () -> String = ::defaultCurrencyCode,
) : SettingsStore {
    constructor(context: Context) : this(context.settingsDataStore)

    override val settings: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            currencyCode = preferences[CurrencyCodeKey] ?: currencyCodeProvider(),
            dailyWaterGoalMl = preferences[DailyWaterGoalKey] ?: 2_500,
            dailyCalorieGoal = preferences[DailyCalorieGoalKey],
            dailyProteinGoalGrams = preferences[DailyProteinGoalKey],
            cupSizeMl = preferences[CupSizeKey] ?: 250,
        )
    }

    override suspend fun update(settings: UserSettings) {
        dataStore.edit { preferences ->
            preferences[CurrencyCodeKey] = settings.currencyCode
            preferences[DailyWaterGoalKey] = settings.dailyWaterGoalMl
            settings.dailyCalorieGoal?.let { preferences[DailyCalorieGoalKey] = it } ?: preferences.remove(DailyCalorieGoalKey)
            settings.dailyProteinGoalGrams?.let { preferences[DailyProteinGoalKey] = it } ?: preferences.remove(DailyProteinGoalKey)
            preferences[CupSizeKey] = settings.cupSizeMl
        }
    }

    private companion object {
        fun defaultCurrencyCode(): String = runCatching {
            Currency.getInstance(Locale.getDefault()).currencyCode
        }.getOrDefault("INR")

        val CurrencyCodeKey = stringPreferencesKey("currency_code")
        val DailyWaterGoalKey = intPreferencesKey("daily_water_goal_ml")
        val DailyCalorieGoalKey = intPreferencesKey("daily_calorie_goal")
        val DailyProteinGoalKey = intPreferencesKey("daily_protein_goal_grams")
        val CupSizeKey = intPreferencesKey("cup_size_ml")
    }
}
