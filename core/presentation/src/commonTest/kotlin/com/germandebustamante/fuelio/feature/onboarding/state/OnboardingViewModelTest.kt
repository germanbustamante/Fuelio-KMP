package com.germandebustamante.fuelio.feature.onboarding.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetDefaultFuelTypeUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetHasCompletedOnboardingUseCase
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import com.germandebustamante.fuelio.feature.onboarding.analytics.OnboardingCompleted
import com.germandebustamante.fuelio.feature.onboarding.analytics.OnboardingSkipped
import com.germandebustamante.fuelio.feature.onboarding.analytics.OnboardingStepViewed
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val setHasCompletedOnboardingUseCase: SetHasCompletedOnboardingUseCase = mock {
        everySuspend { invoke(any()) } returns Unit
    }

    private val setDefaultFuelTypeUseCase: SetDefaultFuelTypeUseCase = mock {
        everySuspend { invoke(any()) } returns Unit
    }

    private val locationPermissionController: LocationPermissionController = mock {
        everySuspend { requestPermission() } returns LocationPermissionState.Granted
    }

    private val analyticsManager: AnalyticsTracking = mock {
        everySuspend { track(any()) } returns Unit
    }

    private lateinit var sut: OnboardingViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - WHEN the ViewModel is created THEN the welcome step is tracked`() = runTest {
        val stepViewedSlot = Capture.slot<OnboardingStepViewed>()
        everySuspend { analyticsManager.track(capture(stepViewedSlot)) } returns Unit

        createSut()
        advanceUntilIdle()

        assertEquals(OnboardingStep.WELCOME, stepViewedSlot.get().step)
    }

    @Test
    fun `onWelcomeNextTapped - WHEN called THEN the state advances to location permission and the step is tracked`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onWelcomeNextTapped()
        advanceUntilIdle()

        assertEquals(OnboardingStep.LOCATION_PERMISSION, sut.state.value.step)
        verifySuspend { analyticsManager.track(OnboardingStepViewed(OnboardingStep.LOCATION_PERMISSION)) }
    }

    @Test
    fun `onRequestLocationPermissionTapped - WHEN called THEN permission is requested and the state advances to default fuel`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onRequestLocationPermissionTapped()
        advanceUntilIdle()

        verifySuspend { locationPermissionController.requestPermission() }
        assertEquals(OnboardingStep.DEFAULT_FUEL, sut.state.value.step)
        verifySuspend { analyticsManager.track(OnboardingStepViewed(OnboardingStep.DEFAULT_FUEL)) }
    }

    @Test
    fun `onSkipLocationPermissionTapped - WHEN called THEN the state advances to default fuel without requesting permission`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onSkipLocationPermissionTapped()
        advanceUntilIdle()

        assertEquals(OnboardingStep.DEFAULT_FUEL, sut.state.value.step)
    }

    @Test
    fun `onFuelTypeSelected - WHEN called THEN the selected fuel type is stored in state`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onFuelTypeSelected(FuelType.DIESEL)
        advanceUntilIdle()

        assertEquals(FuelType.DIESEL, sut.state.value.selectedFuelType)
    }

    @Test
    fun `onFinishTapped - WHEN called THEN the selected fuel and completion flag are persisted and completion is tracked`() = runTest {
        createSut()
        advanceUntilIdle()
        sut.onFuelTypeSelected(FuelType.DIESEL)
        advanceUntilIdle()

        sut.onFinishTapped()
        advanceUntilIdle()

        verifySuspend { setDefaultFuelTypeUseCase(FuelType.DIESEL) }
        verifySuspend { setHasCompletedOnboardingUseCase(true) }
        verifySuspend { analyticsManager.track(OnboardingCompleted) }
    }

    @Test
    fun `onSkipAllTapped - WHEN called THEN the completion flag is persisted and skip is tracked with the current step`() = runTest {
        createSut()
        advanceUntilIdle()
        sut.onWelcomeNextTapped()
        advanceUntilIdle()

        sut.onSkipAllTapped()
        advanceUntilIdle()

        verifySuspend { setHasCompletedOnboardingUseCase(true) }
        verifySuspend { analyticsManager.track(OnboardingSkipped(OnboardingStep.LOCATION_PERMISSION)) }
    }

    private fun createSut() {
        sut = OnboardingViewModel(
            setHasCompletedOnboardingUseCase = setHasCompletedOnboardingUseCase,
            setDefaultFuelTypeUseCase = setDefaultFuelTypeUseCase,
            locationPermissionController = locationPermissionController,
            analyticsManager = analyticsManager,
        )
    }
}
