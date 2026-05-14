package com.germandebustamante.fuelio.feature.list.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.fuelio.core.domain.error.toDomainError
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GasStationsViewModel(
    private val getGasStationByLocationUseCase: GetGasStationsByLocationUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
    private val locationPermissionController: LocationPermissionController,
    private val resolveProvinceByLocationUseCase: ResolveProvinceByLocationUseCase,
) : ViewModel() {

    //region State

    private val _selectedProvince: MutableStateFlow<ProvinceBO?> = MutableStateFlow(null)
    private val _userLocation: MutableStateFlow<LocationPermissionController.Location?> = MutableStateFlow(null)
    private var rawGasStations: List<GasStationBO> = emptyList()
    private var allGasStations: List<GasStationItemVO> = emptyList()

    private val _state = MutableStateFlow(GasStationsUIState())
    val state: StateFlow<GasStationsUIState> = _state.asStateFlow()

    //endregion

    //region Init

    init {
        viewModelScope.launch {
            fetchProvinces()
            fetchProvinceGasStations()
        }
    }

    //endregion

    //region Location

    fun onDetectLocationTapped() {
        viewModelScope.launch {
            when (val result = locationPermissionController.requestPermission()) {
                LocationPermissionState.Granted -> updateLocationAndProvince()
                LocationPermissionState.Denied,
                LocationPermissionState.DeniedAlways -> _state.update { it.withLocationPermission(result) }
                LocationPermissionState.NotDetermined -> Unit
            }
        }
    }

    fun onPermissionRationaleAccepted() {
        viewModelScope.launch {
            _state.update { it.withLocationPermission(null) }
            when (locationPermissionController.requestPermission()) {
                LocationPermissionState.Granted -> updateLocationAndProvince()
                LocationPermissionState.DeniedAlways -> _state.update { it.withLocationPermission(LocationPermissionState.DeniedAlways) }
                else -> Unit
            }
        }
    }

    fun onPermissionDialogDismissed() {
        _state.update { it.withLocationPermission(null) }
    }

    fun onOpenAppSettings() {
        locationPermissionController.openAppSettings()
        _state.update { it.withLocationPermission(null) }
    }

    private suspend fun updateLocationAndProvince() {
        val location = locationPermissionController.getCurrentLocation()
        _userLocation.update { location }
        _selectedProvince.update { resolveProvinceByLocationUseCase(_state.value.provinces, location?.province) }
        rebuildStationsWithCurrentLocation()
    }

    private fun rebuildStationsWithCurrentLocation() {
        if (rawGasStations.isEmpty()) return
        val now = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE)
        _state.update { currentState ->
            allGasStations = buildGasStationItems(rawGasStations, now, currentState.selectedFuelFilter)
            val filtered = allGasStations
                .map { it.withFuelFilter(currentState.selectedFuelFilter) }
                .applySearchQuery(currentState.searchQuery)
            currentState.withSearchQuery(currentState.searchQuery, filtered)
        }
    }

    //endregion

    //region Province

    fun onFilterProvinceToggle(showFilterProvince: Boolean) {
        _state.update { it.withProvinceFilterVisible(showFilterProvince) }
    }

    fun onProvinceSelected(province: ProvinceBO) {
        _selectedProvince.update { province }
    }

    //endregion

    //region Filters

    fun onFuelFilterSelected(filter: FuelFilter) {
        _state.update { state ->
            val stations = allGasStations
                .map { it.withFuelFilter(filter) }
                .applySearchQuery(state.searchQuery)
            state.withFuelFilter(filter, stations)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { state ->
            val stations = allGasStations
                .map { it.withFuelFilter(state.selectedFuelFilter) }
                .applySearchQuery(query)
            state.withSearchQuery(query, stations)
        }
    }

    //endregion

    //region Data fetching

    private suspend fun fetchProvinces() {
        getProvincesUseCase().collect { result ->
            result.fold(
                onSuccess = { provinces ->
                    _state.update { it.withProvinces(provinces) }
                    _selectedProvince.update { resolveProvinceByLocationUseCase(provinces, null) }
                },
                onFailure = ::notifyError,
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun fetchProvinceGasStations() {
        _selectedProvince
            .filterNotNull()
            .onEach { province -> _state.update { it.withLoadingProvince(province) } }
            .flatMapLatest { province -> getGasStationByLocationUseCase(province.id) }
            .collect { result ->
                result.fold(
                    onSuccess = { gasStations ->
                        rawGasStations = gasStations
                        val now = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE)
                        _state.update { currentState ->
                            allGasStations = buildGasStationItems(gasStations, now, currentState.selectedFuelFilter)
                            currentState.withStationsLoaded(allGasStations)
                        }
                    },
                    onFailure = ::notifyError,
                )
            }
    }

    private fun buildGasStationItems(
        gasStations: List<GasStationBO>,
        now: LocalDateTime,
        fuelFilter: FuelFilter,
    ): List<GasStationItemVO> {
        val userLocation = _userLocation.value
        return gasStations
            .map { station ->
                station.toGasStationItemVO(
                    isOpen = station.isOpen(now),
                    distanceInKilometers = userLocation?.let { loc ->
                        distanceBetween(loc.latitude, loc.longitude, station.latitude, station.longitude)
                    },
                    fuelFilter = fuelFilter,
                )
            }
            .sortedWith(compareBy(nullsLast()) { it.distanceInKilometers })
    }

    //endregion

    //region Helpers

    fun onDismissError() {
        _state.update { it.withErrorCleared() }
    }

    private fun notifyError(error: Throwable) {
        _state.update { it.withError(error.toDomainError()) }
    }

    //endregion

    companion object {
        private val SPAIN_TIMEZONE = TimeZone.of("Europe/Madrid")
    }
}
