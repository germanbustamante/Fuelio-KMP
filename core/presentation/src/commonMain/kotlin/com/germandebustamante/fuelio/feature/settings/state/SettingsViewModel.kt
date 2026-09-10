package com.germandebustamante.fuelio.feature.settings.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetDefaultFuelTypeUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetThemeModeUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.feature.common.viewmodel.launchStartupTasks
import com.germandebustamante.fuelio.feature.settings.analytics.DefaultFuelChanged
import com.germandebustamante.fuelio.feature.settings.analytics.SettingsScreenViewed
import com.germandebustamante.fuelio.feature.settings.analytics.ThemeModeChanged
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setDefaultFuelTypeUseCase: SetDefaultFuelTypeUseCase,
    private val navigator: Navigator,
    private val analyticsManager: AnalyticsTracking,
    initialState: SettingsUIState = SettingsUIState(),
) : ViewModel() {

    private val _state = MutableStateFlow(viewModelScope, initialState)

    @NativeCoroutinesState
    val state: StateFlow<SettingsUIState> = _state.asStateFlow()

    init {
        launchStartupTasks(
            { analyticsManager.track(SettingsScreenViewed) },
            { observePreferences() },
        )
    }

    /**
     * Collects rather than reading once: the source of truth is the store, so the screen reflects a
     * write the moment it lands instead of optimistically updating its own state and hoping.
     */
    private suspend fun observePreferences() {
        observeUserPreferencesUseCase().collect { preferences ->
            _state.update { it.withPreferences(preferences.themeMode, preferences.defaultFuelType) }
        }
    }

    fun onThemeModeSelected(themeMode: ThemeMode) {
        viewModelScope.launch {
            setThemeModeUseCase(themeMode)
            analyticsManager.track(ThemeModeChanged(themeMode))
        }
    }

    fun onDefaultFuelSelected(fuelType: FuelType) {
        viewModelScope.launch {
            setDefaultFuelTypeUseCase(fuelType)
            analyticsManager.track(DefaultFuelChanged(fuelType))
        }
    }

    fun onBackTapped() {
        viewModelScope.launch { navigator.navigateUp() }
    }
}
