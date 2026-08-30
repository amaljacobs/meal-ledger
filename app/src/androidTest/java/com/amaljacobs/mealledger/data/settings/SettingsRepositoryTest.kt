package com.amaljacobs.mealledger.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryTest {
    @Test
    fun settingsSurviveRepositoryRecreation() = runBlocking {
        val file = File(
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
            "settings-test-${UUID.randomUUID()}.preferences_pb",
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
        val savedSettings = UserSettings("USD", 3_000, 2_400, 130, 350)

        SettingsRepository(dataStore) { "INR" }.update(savedSettings)

        assertEquals(savedSettings, SettingsRepository(dataStore) { "INR" }.settings.first())
        scope.cancel()
        file.delete()
        Unit
    }
}
