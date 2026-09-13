package com.germandebustamante.fuelio.feature.onboarding.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetDefaultFuelTypeUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetHasCompletedOnboardingUseCase
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.viewmodel.launchStartupTasks
import com.germandebustamante.fuelio.feature.onboarding.analytics.OnboardingCompleted
import com.germandebustamante.fuelio.feature.onboarding.analytics.OnboardingSkipped
import com.germandebustamante.fuelio.feature.onboarding.analytics.OnboardingStepViewed
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * First-run flow: explains the app, asks for location permission and the default fuel, then marks
 * itself done so it never shows again. [AppViewModel] decides whether this screen or the list is the
 * app's root, based on the same persisted flag this ViewModel sets on [onFinish]/[onSkipAll].
 */
class OnboardingViewModel(
    private val setHasCompletedOnboardingUseCase: SetHasCompletedOnboardingUseCase,
    private val setDefaultFuelTypeUseCase: SetDefaultFuelTypeUseCase,
    private val locationPermissionController: LocationPermissionController,
    private val analyticsManager: AnalyticsTracking,
    initialState: OnboardingUIState = OnboardingUIState(),
) : ViewModel() {

    private val _state = MutableStateFlow(viewModelScope, initialState)

    @NativeCoroutinesState
    val state: StateFlow<OnboardingUIState> = _state.asStateFlow()

    init {
        launchStartupTasks({ analyticsManager.track(OnboardingStepViewed(OnboardingStep.WELCOME)) })
    }

    fun onWelcomeNextTapped() {
        viewModelScope.launch { goToStep(OnboardingStep.LOCATION_PERMISSION) }
    }

    fun onRequestLocationPermissionTapped() {
        viewModelScope.launch {
            locationPermissionController.requestPermission()
            goToStep(OnboardingStep.DEFAULT_FUEL)
        }
    }

    fun onSkipLocationPermissionTapped() {
        viewModelScope.launch { goToStep(OnboardingStep.DEFAULT_FUEL) }
    }

    fun onFuelTypeSelected(fuelType: FuelType) {
        _state.update { it.copy(selectedFuelType = fuelType) }
    }

    fun onFinishTapped() {
        viewModelScope.launch {
            setDefaultFuelTypeUseCase(_state.value.selectedFuelType)
            setHasCompletedOnboardingUseCase(true)
            analyticsManager.track(OnboardingCompleted)
        }
    }

    fun onSkipAllTapped() {
        viewModelScope.launch {
            analyticsManager.track(OnboardingSkipped(_state.value.step))
            setHasCompletedOnboardingUseCase(true)
        }
    }

    private suspend fun goToStep(step: OnboardingStep) {
        analyticsManager.track(OnboardingStepViewed(step))
        _state.update { it.copy(step = step) }
    }
}
