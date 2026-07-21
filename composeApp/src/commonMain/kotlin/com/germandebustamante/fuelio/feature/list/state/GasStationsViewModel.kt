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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

class GasStationsViewModel(
    private val getGasStationByLocationUseCase: GetGasStationsByLocationUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
    private val locationPermissionController: LocationPermissionController,
    private val resolveProvinceByLocationUseCase: ResolveProvinceByLocationUseCase,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    //region State

    private val _selectedProvince: MutableStateFlow<ProvinceBO?> = MutableStateFlow(null)
    private val _userLocation: MutableStateFlow<LocationPermissionController.Location?> = MutableStateFlow(null)
    private val _searchQueryFlow: MutableStateFlow<String> = MutableStateFlow("")
    private var rawGasStations: List<GasStationBO> = emptyList()
    private var allGasStations: List<GasStationItemVO> = emptyList()

    private val _state = MutableStateFlow(GasStationsUIState())
    val state: StateFlow<GasStationsUIState> = _state.asStateFlow()

    //endregion

    private fun updateState(transform: (GasStationsUIState) -> GasStationsUIState) {
        _state.update(transform)
    }

    //region Init

    init {
        viewModelScope.launch { fetchProvinces() }
        viewModelScope.launch { fetchProvinceGasStations() }
        viewModelScope.launch {
            // Wait for provinces before resolving location province
            _state.first { it.provinces.isNotEmpty() }
            initLocationPermission()
        }
        viewModelScope.launch { observeSearchQuery() }
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeSearchQuery() {
        _searchQueryFlow
            .debounce(SEARCH_DEBOUNCE_MS.milliseconds)
            .collect { query ->
                val currentState = _state.value
                val stations = withContext(defaultDispatcher) {
                    allGasStations
                        .map { it.withFuelFilter(currentState.selectedFuelFilter) }
                        .applySearchQuery(query)
                        .markCheapest()
                }
                updateState { it.withSearchQuery(query, stations) }
            }
    }

    private suspend fun initLocationPermission() {
        when (locationPermissionController.checkCurrentStatus()) {
            LocationPermissionState.Granted -> updateLocationAndProvince()
            LocationPermissionState.NotDetermined,
            LocationPermissionState.Denied,
            -> {
                if (locationPermissionController.requestPermission() == LocationPermissionState.Granted) {
                    updateLocationAndProvince()
                }
            }
            LocationPermissionState.DeniedAlways -> Unit
        }
    }

    //endregion

    //region Location

    fun onDetectLocationTapped() {
        viewModelScope.launch {
            when (locationPermissionController.requestPermission()) {
                LocationPermissionState.Granted -> updateLocationAndProvince()
                LocationPermissionState.DeniedAlways -> updateState { it.withPermissionDeniedPermanently() }
                else -> Unit
            }
        }
    }

    fun onDismissPermissionSnackbar() {
        updateState { it.withPermissionSnackbarDismissed() }
    }

    fun onOpenAppSettings() {
        locationPermissionController.openAppSettings()
        updateState { it.withPermissionSnackbarDismissed() }
    }

    private suspend fun updateLocationAndProvince() {
        val location = locationPermissionController.getCurrentLocation()
        _userLocation.update { location }
        _selectedProvince.update { resolveProvinceByLocationUseCase(_state.value.provinces, location?.province) }
        rebuildStationsWithCurrentLocation()
    }

    private suspend fun rebuildStationsWithCurrentLocation() {
        if (rawGasStations.isEmpty()) return
        val now = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE)
        val currentState = _state.value
        val built = buildGasStationItems(rawGasStations, now, currentState.selectedFuelFilter)
        val filtered = built
            .map { it.withFuelFilter(currentState.selectedFuelFilter) }
            .applySearchQuery(currentState.searchQuery)
        allGasStations = built
        updateState { it.withSearchQuery(currentState.searchQuery, filtered) }
    }

    //endregion

    //region Province

    fun onFilterProvinceToggle(showFilterProvince: Boolean) {
        updateState { it.withProvinceFilterVisible(showFilterProvince) }
    }

    fun onProvinceSelected(province: ProvinceBO) {
        _selectedProvince.update { province }
    }

    //endregion

    //region Filters

    fun onFuelFilterSelected(filter: FuelFilter) {
        viewModelScope.launch {
            val currentState = _state.value
            val stations = withContext(defaultDispatcher) {
                allGasStations
                    .map { it.withFuelFilter(filter) }
                    .applySearchQuery(currentState.searchQuery)
                    .markCheapest()
            }
            updateState { it.withFuelFilter(filter, stations) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        // Update the text field immediately; filtering is debounced in observeSearchQuery
        updateState { it.copy(searchQuery = query) }
        _searchQueryFlow.value = query
    }

    //endregion

    //region Data fetching

    private suspend fun fetchProvinces() {
        getProvincesUseCase().collect { result ->
            result.fold(
                onSuccess = { provinces ->
                    updateState { it.withProvinces(provinces) }
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
            .onEach { province -> updateState { it.withLoadingProvince(province) } }
            .flatMapLatest { province -> getGasStationByLocationUseCase(province.id) }
            .collect { result ->
                var successStations: List<GasStationBO>? = null
                result.fold(
                    onSuccess = { stations -> successStations = stations },
                    onFailure = ::notifyError,
                )
                successStations?.let { gasStations ->
                    rawGasStations = gasStations
                    val now = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE)
                    val built = buildGasStationItems(gasStations, now, _state.value.selectedFuelFilter)
                    allGasStations = built
                    updateState { it.withStationsLoaded(built) }
                }
            }
    }

    private suspend fun buildGasStationItems(
        gasStations: List<GasStationBO>,
        now: LocalDateTime,
        fuelFilter: FuelFilter,
    ): List<GasStationItemVO> = withContext(defaultDispatcher) {
        val userLocation = _userLocation.value
        gasStations
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
            .markCheapest()
    }

    //endregion

    //region Helpers

    fun onDismissError() {
        updateState { it.withErrorCleared() }
    }

    fun onRefresh() {
        viewModelScope.launch {
            updateState { it.withRefreshing() }
            _selectedProvince.value?.let { province ->
                getGasStationByLocationUseCase(province.id).collect { result ->
                    var successStations: List<GasStationBO>? = null
                    result.fold(
                        onSuccess = { stations -> successStations = stations },
                        onFailure = ::notifyError,
                    )
                    successStations?.let { gasStations ->
                        val now = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE)
                        val built = buildGasStationItems(gasStations, now, _state.value.selectedFuelFilter)
                        allGasStations = built
                        updateState { it.withStationsLoaded(built) }
                    }
                }
            }
        }
    }

    fun onRetry() {
        updateState { it.withErrorCleared() }
        _selectedProvince.value?.let {
            _selectedProvince.update { it }
        }
    }

    fun onItemClick(stationId: String) {
        // TODO: Navigate to detail screen
    }

    fun onToggleFavorite(stationId: String) {
        updateState { it.withFavoriteToggled(stationId) }
    }

    private fun notifyError(error: Throwable) {
        updateState { it.withError(error.toDomainError()) }
    }

    private fun List<GasStationItemVO>.markCheapest(): List<GasStationItemVO> {
        val cheapestPrice = mapNotNull { it.getCurrentFuelPrice() }.minOrNull() ?: return this
        var cheapestMarked = false
        return map { vo ->
            val price = vo.getCurrentFuelPrice()
            val isCheapest = !cheapestMarked && price != null && price == cheapestPrice
            if (isCheapest) cheapestMarked = true
            vo.copy(isCheapest = isCheapest)
        }
    }

    //endregion

    companion object {
        private val SPAIN_TIMEZONE = TimeZone.of("Europe/Madrid")
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
