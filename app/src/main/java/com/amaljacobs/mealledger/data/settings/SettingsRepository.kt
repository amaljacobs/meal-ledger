package com.amaljacobs.mealledger.data.settings

import android.content.Context
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

class SettingsRepository(private val context: Context) : SettingsStore {
    override val settings: Flow<UserSettings> = context.settingsDataStore.data.map { preferences ->
        UserSettings(
            currencyCode = preferences[CurrencyCodeKey] ?: defaultCurrencyCode(),
            dailyWaterGoalMl = preferences[DailyWaterGoalKey] ?: 2_500,
            cupSizeMl = preferences[CupSizeKey] ?: 250,
        )
    }

    override suspend fun update(settings: UserSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[CurrencyCodeKey] = settings.currencyCode
            preferences[DailyWaterGoalKey] = settings.dailyWaterGoalMl
            preferences[CupSizeKey] = settings.cupSizeMl
        }
    }

    private fun defaultCurrencyCode(): String = runCatching {
        Currency.getInstance(Locale.getDefault()).currencyCode
    }.getOrDefault("INR")

    private companion object {
        val CurrencyCodeKey = stringPreferencesKey("currency_code")
        val DailyWaterGoalKey = intPreferencesKey("daily_water_goal_ml")
        val CupSizeKey = intPreferencesKey("cup_size_ml")
    }
}
