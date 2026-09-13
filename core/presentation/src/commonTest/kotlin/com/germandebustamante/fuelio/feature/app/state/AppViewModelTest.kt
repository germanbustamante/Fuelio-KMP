package com.germandebustamante.fuelio.feature.app.state

import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase = mock {
        every { invoke() } returns flowOf(UserPreferencesBO())
    }

    private lateinit var sut: AppViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - GIVEN preferences have not resolved yet THEN hasCompletedOnboarding is null`() = runTest {
        // The Flow is not advanced past this point, so the ViewModel's constructor-time initial
        // state is what we assert on — before the first preferences emission is collected.
        every { observeUserPreferencesUseCase() } returns flowOf(UserPreferencesBO(hasCompletedOnboarding = true))

        sut = AppViewModel(observeUserPreferencesUseCase)

        assertNull(sut.state.value.hasCompletedOnboarding)
    }

    @Test
    fun `init - GIVEN preferences resolve with onboarding completed THEN state reflects it`() = runTest {
        every { observeUserPreferencesUseCase() } returns flowOf(UserPreferencesBO(hasCompletedOnboarding = true))

        sut = AppViewModel(observeUserPreferencesUseCase)
        advanceUntilIdle()

        assertEquals(true, sut.state.value.hasCompletedOnboarding)
    }

    @Test
    fun `init - GIVEN preferences resolve with onboarding not completed THEN state reflects it`() = runTest {
        every { observeUserPreferencesUseCase() } returns flowOf(UserPreferencesBO(hasCompletedOnboarding = false))

        sut = AppViewModel(observeUserPreferencesUseCase)
        advanceUntilIdle()

        assertEquals(false, sut.state.value.hasCompletedOnboarding)
    }

    @Test
    fun `init - WHEN preferences emit a theme mode THEN state reflects it`() = runTest {
        every { observeUserPreferencesUseCase() } returns flowOf(UserPreferencesBO(themeMode = ThemeMode.DARK))

        sut = AppViewModel(observeUserPreferencesUseCase)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, sut.state.value.themeMode)
    }
}
