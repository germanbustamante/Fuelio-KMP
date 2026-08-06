package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.core.util.SPAIN_TIMEZONE
import com.germandebustamante.fuelio.feature.common.viewmodel.launchStartupTasks
import com.germandebustamante.fuelio.feature.detail.analytics.GasStationDetailScreenViewed
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GasStationDetailViewModel(
    private val route: Destination.GasStationDetails,
    private val getGasStation: GetGasStationUseCase,
    private val navigator: Navigator,
    private val analyticsManager: AnalyticsTracking,
    initialState: GasStationDetailUIState = GasStationDetailUIState(),
) : ViewModel() {

    // MutableStateFlow(viewModelScope, …) is the KMP-ObservableViewModel overload: it is what
    // notifies SwiftUI on every emission. A plain kotlinx MutableStateFlow would still work on
    // Android but would never repaint the iOS views.
    private val _state: MutableStateFlow<GasStationDetailUIState> = MutableStateFlow(viewModelScope, initialState)

    @NativeCoroutinesState
    val state: StateFlow<GasStationDetailUIState> = _state.asStateFlow()

    init {
        launchStartupTasks(
            { analyticsManager.track(GasStationDetailScreenViewed(route.gasStationId)) },
            {
                getGasStation(route.gasStationId).collect { gasStation ->
                    val today = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE).dayOfWeek
                    _state.update { it.withGasStationLoaded(gasStation, today) }
                }
            },
        )
    }

    fun onBackClick() {
        viewModelScope.launch { navigator.navigateUp() }
    }
}
