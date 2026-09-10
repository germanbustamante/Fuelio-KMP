package com.germandebustamante.fuelio.feature.list.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.error.toDomainError
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.location.distanceBetween
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetDefaultFuelTypeUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetSavedProvinceUseCase
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.domain.province.usecase.GetProvincesUseCase
import com.germandebustamante.fuelio.core.domain.province.usecase.ResolveProvinceByLocationUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.core.util.SPAIN_TIMEZONE
import com.germandebustamante.fuelio.feature.common.analytics.ApiCallFailed
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import com.germandebustamante.fuelio.feature.common.viewmodel.launchStartupTasks
import com.germandebustamante.fuelio.feature.list.analytics.GasStationSelected
import com.germandebustamante.fuelio.feature.list.analytics.GasStationsScreenViewed
import com.germandebustamante.fuelio.feature.list.analytics.LocationPermissionEvent
import com.germandebustamante.fuelio.feature.list.analytics.LocationPermissionOutcome
import com.germandebustamante.fuelio.feature.list.analytics.ProvinceChanged
import com.germandebustamante.fuelio.feature.list.analytics.SettingsOpened
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

class GasStationsViewModel(
    private val getGasStationByLocationUseCase: GetGasStationsByLocationUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
    private val locationPermissionController: LocationPermissionController,
    private val resolveProvinceByLocationUseCase: ResolveProvinceByLocationUseCase,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val setDefaultFuelTypeUseCase: SetDefaultFuelTypeUseCase,
    private val setSavedProvinceUseCase: SetSavedProvinceUseCase,
    private val navigator: Navigator,
    private val analyticsManager: AnalyticsTracking,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    initialState: GasStationsUIState = GasStationsUIState(),
) : ViewModel() {

    //region State

    private val _selectedProvince: MutableStateFlow<ProvinceBO?> = MutableStateFlow(null)
    private val _userLocation: MutableStateFlow<LocationPermissionController.Location?> = MutableStateFlow(null)
    private val _searchQueryFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var rawGasStations: List<GasStationBO> = emptyList()
    private var allGasStations: List<GasStationItemVO> = emptyList()

    /**
     * Whether the province on screen came from the user's stored choice.
     *
     * An explicit pick outranks geolocation: someone who chose Madrid while driving through
     * Guadalajara should not have it swapped out from under them on the next launch. Location is
     * still read either way — it is what the distance column needs.
     */
    private var hasRestoredSavedProvince: Boolean = false

    /**
     * Read once and cached. Preferences are needed at two points during startup that race each other
     * (picking the initial province, and applying the default fuel), and re-collecting the flow in
     * both would be two separate reads of the same file.
     */
    private var storedPreferences: UserPreferencesBO? = null

    /**
     * Whether the user has picked a fuel on this screen.
     *
     * Restoring the stored default races the user: reading it is file I/O launched concurrently with
     * the rest of startup, so on a slow read it can land *after* a tap and silently put the filter
     * back. As with the province, an explicit choice always wins.
     */
    private var hasUserSelectedFuel: Boolean = false

    // MutableStateFlow(viewModelScope, …) is the KMP-ObservableViewModel overload: it is what
    // notifies SwiftUI on every emission. The private flows above stay plain kotlinx flows — they
    // are internal plumbing that Swift never observes.
    private val _state = MutableStateFlow(viewModelScope, initialState)

    @NativeCoroutinesState
    val state: StateFlow<GasStationsUIState> = _state.asStateFlow()

    //endregion

    private fun updateState(transform: (GasStationsUIState) -> GasStationsUIState) {
        _state.update(transform)
    }

    //region Init

    init {
        launchStartupTasks(
            { analyticsManager.track(GasStationsScreenViewed) },
            { applyStoredFuelPreference() },
            { fetchProvinces() },
            { fetchProvinceGasStations() },
            {
                // Wait for provinces before resolving location province
                _state.first { it.provinces.isNotEmpty() }
                initLocationPermission()
            },
            { observeSearchQuery() },
        )
    }

    private suspend fun readStoredPreferences(): UserPreferencesBO =
        storedPreferences ?: observeUserPreferencesUseCase().first().also { storedPreferences = it }

    private suspend fun applyStoredFuelPreference() {
        val storedFilter = readStoredPreferences().defaultFuelType.toFuelFilter()
        if (hasUserSelectedFuel) return
        applyFuelFilter(storedFilter)
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
            analyticsManager.track(LocationPermissionEvent(LocationPermissionOutcome.REQUESTED))
            when (locationPermissionController.requestPermission()) {
                LocationPermissionState.Granted -> {
                    analyticsManager.track(LocationPermissionEvent(LocationPermissionOutcome.GRANTED))
                    updateLocationAndProvince()
                }
                LocationPermissionState.DeniedAlways -> {
                    analyticsManager.track(LocationPermissionEvent(LocationPermissionOutcome.DENIED_PERMANENTLY))
                    updateState { it.withPermissionDeniedPermanently() }
                }
                else -> analyticsManager.track(LocationPermissionEvent(LocationPermissionOutcome.DENIED))
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
        // The location is always applied — distances need it — but the province only when the user
        // has no stored choice to override.
        if (!hasRestoredSavedProvince) {
            _selectedProvince.update { resolveProvinceByLocationUseCase(_state.value.provinces, location?.province) }
        }
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
        // An explicit pick is what makes the choice stick across launches, so from here on
        // geolocation must not override it.
        hasRestoredSavedProvince = true
        viewModelScope.launch {
            setSavedProvinceUseCase(province.id)
            analyticsManager.track(ProvinceChanged(province.id, province.name))
        }
    }

    //endregion

    //region Filters

    fun onFuelFilterSelected(filter: FuelFilter) {
        hasUserSelectedFuel = true
        viewModelScope.launch {
            applyFuelFilter(filter)
            setDefaultFuelTypeUseCase(filter.toFuelType())
        }
    }

    /**
     * Shared by the user's selection and by restoring the stored default at startup — the latter
     * must not write the preference straight back out.
     */
    private suspend fun applyFuelFilter(filter: FuelFilter) {
        val currentState = _state.value
        val stations = withContext(defaultDispatcher) {
            allGasStations
                .map { it.withFuelFilter(filter) }
                .applySearchQuery(currentState.searchQuery)
                .markCheapest()
        }
        updateState { it.withFuelFilter(filter, stations) }
    }

    fun onSearchQueryChanged(query: String) {
        // Update the text field immediately; filtering is debounced in observeSearchQuery
        updateState { it.copy(searchQuery = query) }
        _searchQueryFlow.update { query }
    }

    //endregion

    //region Data fetching

    private suspend fun fetchProvinces() {
        getProvincesUseCase().collect { result ->
            result.fold(
                onSuccess = { provinces ->
                    updateState { it.withProvinces(provinces) }
                    _selectedProvince.update { selectInitialProvince(provinces) }
                },
                onFailure = { notifyError(OPERATION_FETCH_PROVINCES, it) },
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun fetchProvinceGasStations() {
        _selectedProvince
            .filterNotNull()
            .flatMapLatest { province ->
                merge(flowOf(FetchTrigger.ProvinceSelected), _refreshTrigger.map { FetchTrigger.ManualRefresh })
                    .map { trigger -> province to trigger }
            }
            .onEach { (province, trigger) ->
                updateState {
                    when (trigger) {
                        FetchTrigger.ProvinceSelected -> it.withLoadingProvince(province)
                        FetchTrigger.ManualRefresh -> it.withRefreshing()
                    }
                }
            }
            .flatMapLatest { (province, _) -> getGasStationByLocationUseCase(province.id) }
            .collect { result ->
                result.fold(
                    onSuccess = { (gasStations, isFromCache) ->
                        rawGasStations = gasStations
                        val now = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE)
                        val built = buildGasStationItems(gasStations, now, _state.value.selectedFuelFilter)
                        allGasStations = built
                        // Re-map against the filter as it stands *now*, not the one read before the
                        // build: restoring the stored default is concurrent with this load, so the
                        // filter can change while `buildGasStationItems` is off on another dispatcher.
                        // Without this the list can show prices for a fuel the chip no longer says.
                        updateState { state ->
                            state.withStationsLoaded(built.map { it.withFuelFilter(state.selectedFuelFilter) }.markCheapest(), isFromCache)
                        }
                    },
                    onFailure = { notifyError(OPERATION_FETCH_STATIONS, it) },
                )
            }
    }

    /**
     * The stored province wins when it still exists in the list; otherwise this falls back to the
     * previous behaviour of picking the first one, which the location task may then refine.
     *
     * A stored id that no longer resolves is a real case — the upstream province list is not ours —
     * and silently falling back beats showing an empty screen.
     */
    private suspend fun selectInitialProvince(provinces: List<ProvinceBO>): ProvinceBO? {
        val savedProvinceId = readStoredPreferences().savedProvinceId
        val savedProvince = provinces.firstOrNull { it.id == savedProvinceId }
        hasRestoredSavedProvince = savedProvince != null
        return savedProvince ?: resolveProvinceByLocationUseCase(provinces, null)
    }

    private enum class FetchTrigger { ProvinceSelected, ManualRefresh }

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

    fun onDismissStaleDataError() {
        updateState { it.withStaleDataErrorDismissed() }
    }

    fun onRefresh() {
        viewModelScope.launch { _refreshTrigger.emit(Unit) }
    }

    fun onRetry() {
        updateState { it.withErrorCleared() }
        viewModelScope.launch { _refreshTrigger.emit(Unit) }
    }

    fun onItemClick(stationId: String) {
        viewModelScope.launch {
            analyticsManager.track(GasStationSelected(stationId))
            navigator.navigate(Destination.GasStationDetails(stationId))
        }
    }

    fun onSettingsTapped() {
        viewModelScope.launch {
            analyticsManager.track(SettingsOpened)
            navigator.navigate(Destination.Settings)
        }
    }

    fun onToggleFavorite(stationId: String) {
        updateState { it.withFavoriteToggled(stationId) }
    }

    private fun notifyError(operation: String, error: Throwable) {
        val domainError = error.toDomainError()
        trackApiCallFailed(operation, domainError)
        updateState { state ->
            if (state.gasStations.isNotEmpty()) state.withStaleDataError(domainError) else state.withError(domainError)
        }
    }

    private fun trackApiCallFailed(operation: String, domainError: DomainError) {
        viewModelScope.launch {
            analyticsManager.track(
                ApiCallFailed(
                    screenName = GasStationsScreenViewed.SCREEN_NAME,
                    operation = operation,
                    errorType = domainError::class.simpleName ?: "Unknown",
                    errorMessage = domainError.message,
                ),
            )
        }
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
        const val SEARCH_DEBOUNCE_MS = 300L
        private const val OPERATION_FETCH_PROVINCES = "fetch_provinces"
        private const val OPERATION_FETCH_STATIONS = "fetch_stations"
    }
}
