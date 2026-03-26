package com.germandebustamante.fuelio.feature.list.state

sealed interface GasStationsUIState {
    data object Loading : GasStationsUIState

    data class Success(val gasStations: List<GasStationItemVO>) : GasStationsUIState

    data class Error(val exception: Throwable) : GasStationsUIState
}