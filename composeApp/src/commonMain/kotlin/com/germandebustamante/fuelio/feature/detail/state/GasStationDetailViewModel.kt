package com.germandebustamante.fuelio.feature.detail.state

import androidx.lifecycle.ViewModel
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GasStationDetailViewModel(
    private val route: Destination.GasStationDetails,
): ViewModel() {

    private val _state : MutableStateFlow<GasStationDetailUIState?> = MutableStateFlow(null)
    val state: StateFlow<GasStationDetailUIState?> = _state.asStateFlow()

    init {
        //Get Gas station by id
    }
}