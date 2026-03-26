package com.germandebustamante.fuelio.feature.list.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.data.gasstation.repository.GasStationRepositoryImpl
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GasStationsViewModel(getGasStationByLocationUseCase: GetGasStationsByLocationUseCase) : ViewModel() {

    val state: StateFlow<GasStationsUIState> = getGasStationByLocationUseCase().map { result ->
        result.fold(
            onSuccess = { gasStations -> GasStationsUIState.Success(gasStations = gasStations.map { GasStationItemVO(it) }) },
            onFailure = { GasStationsUIState.Error(it) })
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), GasStationsUIState.Loading
    )


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GasStationsViewModel(GetGasStationsByLocationUseCase(GasStationRepositoryImpl()))
            }
        }
    }


}