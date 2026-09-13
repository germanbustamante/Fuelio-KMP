package com.germandebustamante.fuelio.feature.map.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.error.toDomainError
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.common.analytics.ApiCallFailed
import com.germandebustamante.fuelio.feature.common.viewmodel.launchStartupTasks
import com.germandebustamante.fuelio.feature.map.analytics.MapMarkerSelected
import com.germandebustamante.fuelio.feature.map.analytics.MapScreenViewed
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Its own province resolution, independent of [com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel]:
 * nav args stay plain identifiers (CLAUDE.md), so this reads the same stored preference rather than
 * receiving the list's already-built station VOs across the navigation boundary.
 */
class MapViewModel(
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val getGasStationsByLocationUseCase: GetGasStationsByLocationUseCase,
    private val navigator: Navigator,
    private val analyticsManager: AnalyticsTracking,
    initialState: MapUIState = MapUIState(),
) : ViewModel() {

    private val _state = MutableStateFlow(viewModelScope, initialState)

    @NativeCoroutinesState
    val state: StateFlow<MapUIState> = _state.asStateFlow()

    init {
        launchStartupTasks(
            { analyticsManager.track(MapScreenViewed) },
            { observeMarkers() },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeMarkers() {
        observeUserPreferencesUseCase()
            .map { it.savedProvinceId }
            .flatMapLatest { provinceId -> stationsFlow(provinceId) }
            .collect { result ->
                result.fold(
                    onSuccess = { stations -> onStationsLoaded(stations) },
                    onFailure = { notifyError(it) },
                )
            }
    }

    private fun stationsFlow(provinceId: String?): Flow<Result<List<GasStationBO>>> {
        if (provinceId == null) return flowOf(Result.success(emptyList()))
        return getGasStationsByLocationUseCase(provinceId).map { it.map { result -> result.stations } }
    }

    private fun onStationsLoaded(stations: List<GasStationBO>) {
        _state.update { it.withMarkersLoaded(stations.map { station -> station.toMapMarkerVO() }) }
    }

    fun onMarkerSelected(gasStationId: String) {
        viewModelScope.launch {
            analyticsManager.track(MapMarkerSelected(gasStationId))
            navigator.navigate(Destination.GasStationDetails(gasStationId))
        }
    }

    fun onBackClick() {
        viewModelScope.launch { navigator.navigateUp() }
    }

    private fun notifyError(error: Throwable) {
        val domainError = error.toDomainError()
        viewModelScope.launch {
            analyticsManager.track(
                ApiCallFailed(
                    screenName = MapScreenViewed.SCREEN_NAME,
                    operation = OPERATION_FETCH_STATIONS,
                    errorType = domainError::class.simpleName.orEmpty(),
                    errorMessage = domainError.message.orEmpty(),
                ),
            )
        }
        _state.update { it.withError(domainError) }
    }

    private companion object {
        const val OPERATION_FETCH_STATIONS = "fetch_stations"
    }
}
