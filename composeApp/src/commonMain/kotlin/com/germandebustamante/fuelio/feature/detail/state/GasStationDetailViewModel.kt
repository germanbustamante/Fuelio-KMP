package com.germandebustamante.fuelio.feature.detail.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.fuelio.core.analytics.AnalyticsManager
import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.core.util.SPAIN_TIMEZONE
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime

class GasStationDetailViewModel(
    private val route: Destination.GasStationDetails,
    private val getGasStation: GetGasStationUseCase,
    private val navigator: Navigator,
    private val analyticsManager: AnalyticsManager,
) : ViewModel() {

    private val _state: MutableStateFlow<GasStationDetailUIState> = MutableStateFlow(GasStationDetailUIState())
    val state: StateFlow<GasStationDetailUIState> = _state.asStateFlow()

    init {
        analyticsManager.track(
            Trace.Screen(
                screenName = "gas_station_detail",
                targets = listOf(AnalyticsProviderType.FIREBASE),
                params = mapOf("gas_station_id" to route.gasStationId),
            )
        )

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
