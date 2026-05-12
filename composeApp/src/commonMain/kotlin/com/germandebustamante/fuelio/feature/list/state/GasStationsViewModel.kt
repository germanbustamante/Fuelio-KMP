package com.germandebustamante.fuelio.feature.list.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.fuelio.core.domain.error.toDomainError
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.location.distanceBetween
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.domain.province.usecase.GetProvincesUseCase
import com.germandebustamante.fuelio.core.domain.province.usecase.ResolveProvinceByLocationUseCase
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val SPAIN_TIMEZONE = TimeZone.of("Europe/Madrid")

class GasStationsViewModel(
    private val getGasStationByLocationUseCase: GetGasStationsByLocationUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
    private val locationPermissionController: LocationPermissionController,
    private val resolveProvinceByLocationUseCase: ResolveProvinceByLocationUseCase,
) : ViewModel() {

    private val _selectedProvince: MutableStateFlow<ProvinceBO?> = MutableStateFlow(null)
    private val _userLocation: MutableStateFlow<LocationPermissionController.Location?> = MutableStateFlow(null)

    private val _state = MutableStateFlow(GasStationsUIState())
    val state: StateFlow<GasStationsUIState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            fetchProvinces()
            fetchProvinceGasStations()
        }
    }

    fun onDetectLocationTapped() {
        viewModelScope.launch {
            when (val permissionResult = locationPermissionController.requestPermission()) {
                LocationPermissionState.Granted -> {
                    val location = locationPermissionController.getCurrentLocation()
                    _userLocation.update { location }
                    _selectedProvince.update { resolveProvinceByLocationUseCase(_state.value.provinces, location?.province) }
                }
                LocationPermissionState.Denied,
                LocationPermissionState.DeniedAlways -> {
                    _state.update { it.copy(locationPermissionState = permissionResult) }
                }
                LocationPermissionState.NotDetermined -> Unit
            }
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
                    _selectedProvince.update { resolveProvinceByLocationUseCase(provinces, null) }
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
                        val now = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE)
                        val userLocation = _userLocation.value
                        _state.update {
                            it.copy(
                                gasStations = gasStations
                                    .map { station ->
                                        station.toGasStationItemVO(
                                            isOpen = station.isOpen(now),
                                            distanceInKilometers = userLocation?.let { loc ->
                                                distanceBetween(loc.latitude, loc.longitude, station.latitude, station.longitude)
                                            }
                                        )
                                    }
                                    .sortedWith(compareBy(nullsLast()) { it.distanceInKilometers }),
                                isLoading = false
                            )
                        }
                    },
                    onFailure = this@GasStationsViewModel::notifyError
                )
            }
    }

    fun onPermissionRationaleAccepted() {
        viewModelScope.launch {
            _state.update { it.copy(locationPermissionState = null) }
            val permissionResult = locationPermissionController.requestPermission()
            when (permissionResult) {
                LocationPermissionState.Granted -> {
                    val location = locationPermissionController.getCurrentLocation()
                    _userLocation.update { location }
                    _selectedProvince.update { resolveProvinceByLocationUseCase(_state.value.provinces, location?.province) }
                }
                LocationPermissionState.DeniedAlways -> {
                    _state.update { it.copy(locationPermissionState = LocationPermissionState.DeniedAlways) }
                }
                else -> Unit
            }
        }
    }

    fun onPermissionDialogDismissed() {
        _state.update { it.copy(locationPermissionState = null) }
    }

    fun onOpenAppSettings() {
        locationPermissionController.openAppSettings()
        _state.update { it.copy(locationPermissionState = null) }
    }

    private fun notifyError(error: Throwable) {
        _state.update { it.copy(error = error.toDomainError(), isLoading = false) }
    }
}
