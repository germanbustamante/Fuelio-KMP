package com.germandebustamante.fuelio.feature.settings.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetDefaultFuelTypeUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetThemeModeUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.feature.settings.analytics.DefaultFuelChanged
import com.germandebustamante.fuelio.feature.settings.analytics.SettingsScreenViewed
import com.germandebustamante.fuelio.feature.settings.analytics.ThemeModeChanged
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase = mock {
        every { invoke() } returns flowOf(UserPreferencesBO())
    }

    private val setThemeModeUseCase: SetThemeModeUseCase = mock {
        everySuspend { invoke(any()) } returns Unit
    }

    private val setDefaultFuelTypeUseCase: SetDefaultFuelTypeUseCase = mock {
        everySuspend { invoke(any()) } returns Unit
    }

    private val navigator: Navigator = mock {
        everySuspend { navigateUp() } returns Unit
    }

    private val analyticsManager: AnalyticsTracking = mock {
        everySuspend { track(any()) } returns Unit
    }

    private lateinit var sut: SettingsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - GIVEN stored preferences WHEN initialized THEN they are shown and loading clears`() = runTest {
        every { observeUserPreferencesUseCase() } returns
            flowOf(UserPreferencesBO(defaultFuelType = FuelType.DIESEL, themeMode = ThemeMode.DARK))

        createSut()
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, sut.state.value.themeMode)
        assertEquals(FuelType.DIESEL, sut.state.value.defaultFuelType)
        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `init - GIVEN the screen opens THEN the screen view is tracked`() = runTest {
        createSut()
        advanceUntilIdle()

        verifySuspend { analyticsManager.track(SettingsScreenViewed) }
    }

    @Test
    fun `onThemeModeSelected - GIVEN a mode WHEN selected THEN it is persisted and tracked`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onThemeModeSelected(ThemeMode.LIGHT)
        advanceUntilIdle()

        verifySuspend { setThemeModeUseCase(ThemeMode.LIGHT) }
        verifySuspend { analyticsManager.track(ThemeModeChanged(ThemeMode.LIGHT)) }
    }

    @Test
    fun `onThemeModeSelected - GIVEN a mode WHEN persisted THEN the state follows the store not the tap`() = runTest {
        // The store is the source of truth: the screen must not optimistically update itself, or a
        // write that fails would leave the UI claiming a preference that was never saved.
        val preferences = MutableStateFlow(UserPreferencesBO())
        every { observeUserPreferencesUseCase() } returns preferences
        createSut()
        advanceUntilIdle()

        sut.onThemeModeSelected(ThemeMode.DARK)
        advanceUntilIdle()
        assertEquals(ThemeMode.SYSTEM, sut.state.value.themeMode)

        preferences.value = UserPreferencesBO(themeMode = ThemeMode.DARK)
        advanceUntilIdle()
        assertEquals(ThemeMode.DARK, sut.state.value.themeMode)
    }

    @Test
    fun `onDefaultFuelSelected - GIVEN a fuel WHEN selected THEN it is persisted and tracked`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onDefaultFuelSelected(FuelType.DIESEL_PREMIUM)
        advanceUntilIdle()

        verifySuspend { setDefaultFuelTypeUseCase(FuelType.DIESEL_PREMIUM) }
        verifySuspend { analyticsManager.track(DefaultFuelChanged(FuelType.DIESEL_PREMIUM)) }
    }

    @Test
    fun `onBackTapped - GIVEN the screen WHEN back is tapped THEN navigation goes through the navigator`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onBackTapped()
        advanceUntilIdle()

        verifySuspend { navigator.navigateUp() }
    }

    private fun createSut(initialState: SettingsUIState = SettingsUIState()) {
        sut = SettingsViewModel(
            observeUserPreferencesUseCase = observeUserPreferencesUseCase,
            setThemeModeUseCase = setThemeModeUseCase,
            setDefaultFuelTypeUseCase = setDefaultFuelTypeUseCase,
            navigator = navigator,
            analyticsManager = analyticsManager,
            initialState = initialState,
        )
    }
}
