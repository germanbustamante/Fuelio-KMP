package com.germandebustamante.fuelio.data.preferences.repository

import app.cash.turbine.test
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.data.preferences.local.UserPreferencesLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferencesRepositoryImplTest {

    @Test
    fun `observe - GIVEN nothing stored WHEN observed THEN the defaults are emitted`() = runTest {
        createSut().observe().test {
            assertEquals(UserPreferencesBO(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setDefaultFuelType - GIVEN a fuel WHEN stored THEN observers see it without re-querying`() = runTest {
        val sut = createSut()

        sut.observe().test {
            assertEquals(FuelType.GASOLINE_95, awaitItem().defaultFuelType)

            sut.setDefaultFuelType(FuelType.DIESEL)

            assertEquals(FuelType.DIESEL, awaitItem().defaultFuelType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSavedProvinceId - GIVEN a province WHEN stored THEN it replaces the null default`() = runTest {
        val sut = createSut()

        sut.setSavedProvinceId(PROVINCE_ID)

        sut.observe().test {
            assertEquals(PROVINCE_ID, awaitItem().savedProvinceId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setThemeMode - GIVEN a mode WHEN stored THEN it overrides the system default`() = runTest {
        val sut = createSut()

        sut.setThemeMode(ThemeMode.DARK)

        sut.observe().test {
            assertEquals(ThemeMode.DARK, awaitItem().themeMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `set - GIVEN several preferences WHEN stored THEN each is independent of the others`() = runTest {
        val sut = createSut()

        sut.setDefaultFuelType(FuelType.DIESEL_PREMIUM)
        sut.setThemeMode(ThemeMode.LIGHT)

        sut.observe().test {
            val preferences = awaitItem()
            assertEquals(FuelType.DIESEL_PREMIUM, preferences.defaultFuelType)
            assertEquals(ThemeMode.LIGHT, preferences.themeMode)
            assertEquals(null, preferences.savedProvinceId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createSut() = UserPreferencesRepositoryImpl(InMemoryUserPreferencesLocalDataSource())

    /**
     * Stands in for DataStore rather than writing a real `.preferences_pb`: the factory allows one
     * live instance per file, so tests sharing a path would interfere with each other, and the file
     * would leak between runs.
     */
    private class InMemoryUserPreferencesLocalDataSource : UserPreferencesLocalDataSource {
        private val preferences = MutableStateFlow(UserPreferencesBO())

        override fun observe(): Flow<UserPreferencesBO> = preferences

        override suspend fun setDefaultFuelType(fuelType: FuelType) {
            preferences.update { it.copy(defaultFuelType = fuelType) }
        }

        override suspend fun setSavedProvinceId(provinceId: String) {
            preferences.update { it.copy(savedProvinceId = provinceId) }
        }

        override suspend fun setThemeMode(themeMode: ThemeMode) {
            preferences.update { it.copy(themeMode = themeMode) }
        }
    }

    private companion object {
        const val PROVINCE_ID = "28"
    }
}
