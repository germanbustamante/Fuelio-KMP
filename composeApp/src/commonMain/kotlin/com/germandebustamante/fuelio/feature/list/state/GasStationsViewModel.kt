package com.germandebustamante.fuelio.feature.list.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.fuelio.core.domain.error.toDomainError
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.domain.province.usecase.GetProvincesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GasStationsViewModel(
    private val getGasStationByLocationUseCase: GetGasStationsByLocationUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
) : ViewModel() {

    private val _selectedProvince: MutableStateFlow<ProvinceBO?> = MutableStateFlow(null)

    private val _state = MutableStateFlow(GasStationsUIState())
    val state: StateFlow<GasStationsUIState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            fetchProvinces()
            fetchProvinceGasStations()
        }
    }

    fun onDismissError() {
        _state.update { it.copy(error = null) }
    }

    fun onFilterProvinceToggle(showFilterProvince: Boolean) {
        _state.update { it.copy(showFilterProvince = showFilterProvince) }
    }

    fun onProvinceSelected(province: ProvinceBO) {
        _selectedProvince.update { province }
    }

    private suspend fun fetchProvinces() {
        getProvincesUseCase().collect { result ->
            result.fold(
                onSuccess = { provinces ->
                    _state.update { it.copy(provinces = provinces) }
                    _selectedProvince.update { provinces.firstOrNull() }
                },
                onFailure = this@GasStationsViewModel::notifyError
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun fetchProvinceGasStations() {
        _selectedProvince
            .filterNotNull()
            .onEach { province ->
                _state.update { it.copy(isLoading = true, selectedProvince = province) }
            }
            .flatMapLatest { province -> getGasStationByLocationUseCase(province.id) }
            .collect { result ->
                result.fold(
                    onSuccess = { gasStations ->
                        _state.update {
                            it.copy(
                                gasStations = gasStations.map(::GasStationItemVO),
                                isLoading = false
                            )
                        }
                    },
                    onFailure = this@GasStationsViewModel::notifyError
                )
            }
    }

    private fun notifyError(error: Throwable) {
        _state.update { it.copy(error = error.toDomainError(), isLoading = false) }
    }
}
