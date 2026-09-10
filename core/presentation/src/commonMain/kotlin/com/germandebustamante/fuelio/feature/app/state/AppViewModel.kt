package com.germandebustamante.fuelio.feature.app.state

import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.feature.common.viewmodel.launchStartupTasks
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the state that belongs to the app shell rather than to any screen — today just the theme.
 *
 * It exists because the theme has to be applied *above* the navigation graph, where no screen
 * ViewModel is in scope: on Android around `FuelioNavHost`, on iOS as `.preferredColorScheme` on
 * `RootView`. Both platforms therefore need the same shared answer, which is exactly the kind of
 * thing `:core:presentation` is for.
 */
class AppViewModel(private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase, initialState: AppUIState = AppUIState()) :
    ViewModel() {

    private val _state = MutableStateFlow(viewModelScope, initialState)

    @NativeCoroutinesState
    val state: StateFlow<AppUIState> = _state.asStateFlow()

    init {
        launchStartupTasks({ observeThemeMode() })
    }

    private suspend fun observeThemeMode() {
        observeUserPreferencesUseCase().collect { preferences ->
            _state.update { it.copy(themeMode = preferences.themeMode) }
        }
    }
}
