package com.germandebustamante.fuelio.feature.detail.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GasStationDetailViewModel(
    private val route: Destination.GasStationDetails,
    private val getGasStation: GetGasStationUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<GasStationDetailUIState?> = MutableStateFlow(null)
    val state: StateFlow<GasStationDetailUIState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getGasStation(route.gasStationId).collect { gasStation ->
                _state.update { gasStation?.let { GasStationDetailUIState(it) } }
            }
        }
    }
}