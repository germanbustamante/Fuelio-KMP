package com.germandebustamante.fuelio.feature.list.state

import com.germandebustamante.fuelio.core.domain.error.DomainError

sealed interface GasStationsUIState {
    data object Loading : GasStationsUIState

    data class Success(val gasStations: List<GasStationItemVO>) : GasStationsUIState

    data class Error(val error: DomainError) : GasStationsUIState
}