package com.germandebustamante.fuelio.feature.detail.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.core.util.SPAIN_TIMEZONE
import com.germandebustamante.fuelio.feature.detail.analytics.GasStationDetailScreenViewed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GasStationDetailViewModel(
    private val route: Destination.GasStationDetails,
    private val getGasStation: GetGasStationUseCase,
    private val navigator: Navigator,
    private val analyticsManager: AnalyticsTracking,
) : ViewModel() {

    private val _state: MutableStateFlow<GasStationDetailUIState> = MutableStateFlow(GasStationDetailUIState())
    val state: StateFlow<GasStationDetailUIState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            analyticsManager.track(GasStationDetailScreenViewed(route.gasStationId))
        }

        viewModelScope.launch {
            getGasStation(route.gasStationId).collect { gasStation ->
                val today = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE).dayOfWeek
                _state.update { it.withGasStationLoaded(gasStation, today) }
            }
        }
    }



    fun onBackClick() {
        viewModelScope.launch { navigator.navigateUp() }
    }
}
