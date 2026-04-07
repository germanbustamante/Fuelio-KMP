package com.germandebustamante.fuelio.feature.list.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.fuelio.core.domain.error.toDomainError
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
class GasStationsViewModel(getGasStationByLocationUseCase: GetGasStationsByLocationUseCase) : ViewModel() {

    val state: StateFlow<GasStationsUIState> = getGasStationByLocationUseCase().map { result ->
        result.fold(
            onSuccess = { gasStations -> GasStationsUIState.Success(gasStations = gasStations.map { GasStationItemVO(it) }) },
            onFailure = { GasStationsUIState.Error(it.toDomainError()) })
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), GasStationsUIState.Loading
    )
}