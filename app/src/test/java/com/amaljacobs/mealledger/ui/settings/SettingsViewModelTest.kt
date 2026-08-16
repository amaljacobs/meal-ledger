package com.amaljacobs.mealledger.ui.settings

import com.amaljacobs.mealledger.data.settings.SettingsStore
import com.amaljacobs.mealledger.data.settings.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveCompletesAndAllowsAnotherUpdate() = runTest(dispatcher) {
        val store = FakeSettingsStore()
        val viewModel = SettingsViewModel(store)
        runCurrent()

        viewModel.update { it.copy(dailyWaterGoalMl = "3000") }
        viewModel.save()
        runCurrent()

        assertFalse(viewModel.state.value.saving)
        assertEquals(3_000, store.savedSettings.dailyWaterGoalMl)

        viewModel.update { it.copy(cupSizeMl = "350") }
        viewModel.save()
        runCurrent()

        assertFalse(viewModel.state.value.saving)
        assertEquals(350, store.savedSettings.cupSizeMl)
    }

    @Test
    fun saveFailureReenablesTheFormAndShowsAnError() = runTest(dispatcher) {
        val viewModel = SettingsViewModel(FakeSettingsStore(throwOnUpdate = true))
        runCurrent()

        viewModel.save()
        runCurrent()

        assertFalse(viewModel.state.value.saving)
        assertEquals("Could not save settings. Please try again.", viewModel.state.value.error)
    }
}

private class FakeSettingsStore(
    private val throwOnUpdate: Boolean = false,
) : SettingsStore {
    private val values = MutableStateFlow(UserSettings())
    override val settings: Flow<UserSettings> = values
    var savedSettings: UserSettings = values.value
        private set

    override suspend fun update(settings: UserSettings) {
        if (throwOnUpdate) error("Storage unavailable")
        savedSettings = settings
        values.value = settings
    }
}
