package com.germandebustamante.fuelio.data.preferences.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.data.local.datastore.createPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises the real DataStore, not a fake: the mapping this covers (enum names in, enum values out,
 * defaults for absent keys) is the part that would silently break on an upgrade.
 *
 * Each test gets its own file — `PreferenceDataStoreFactory` permits only one live instance per
 * path, so a shared one would fail as soon as two tests overlapped.
 */
class UserPreferencesLocalDataSourceImplTest {

    @Test
    fun `observe - GIVEN an empty store WHEN read THEN every field falls back to its default`() = runTest {
        val sut = UserPreferencesLocalDataSourceImpl(temporaryDataStore())

        assertEquals(UserPreferencesBO(), sut.observe().first())
    }

    @Test
    fun `set - GIVEN every preference written WHEN read back THEN all three round-trip`() = runTest {
        val sut = UserPreferencesLocalDataSourceImpl(temporaryDataStore())

        sut.setDefaultFuelType(FuelType.DIESEL_PREMIUM)
        sut.setSavedProvinceId(PROVINCE_ID)
        sut.setThemeMode(ThemeMode.DARK)

        assertEquals(
            UserPreferencesBO(FuelType.DIESEL_PREMIUM, PROVINCE_ID, ThemeMode.DARK),
            sut.observe().first(),
        )
    }

    @Test
    fun `observe - GIVEN an enum constant that no longer exists WHEN read THEN it degrades to the default`() = runTest {
        val dataStore = temporaryDataStore()
        // Simulates upgrading from a build that had a constant this one dropped: storing enums by
        // name means the old value is still sitting in the file.
        dataStore.edit { it[stringPreferencesKey("default_fuel_type")] = "LPG" }

        val preferences = UserPreferencesLocalDataSourceImpl(dataStore).observe().first()

        assertEquals(FuelType.GASOLINE_95, preferences.defaultFuelType)
    }

    private fun temporaryDataStore() = createPreferencesDataStore {
        val directory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "fuelio-prefs-${Random.nextLong()}"
        FileSystem.SYSTEM.createDirectories(directory)
        (directory / "test.preferences_pb").toString()
    }

    private companion object {
        const val PROVINCE_ID = "28"
    }
}
