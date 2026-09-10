package com.germandebustamante.fuelio.feature.favorites.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ObserveFavoriteStationsUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ToggleFavoriteStationUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.core.util.SPAIN_TIMEZONE
import com.germandebustamante.fuelio.feature.common.viewmodel.launchStartupTasks
import com.germandebustamante.fuelio.feature.favorites.analytics.FavoritesScreenViewed
import com.germandebustamante.fuelio.feature.list.analytics.GasStationSelected
import com.germandebustamante.fuelio.feature.list.state.GasStationItemVO
import com.germandebustamante.fuelio.feature.list.state.toFuelFilter
import com.germandebustamante.fuelio.feature.list.state.toGasStationItemVO
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FavoritesViewModel(
    private val observeFavoriteStationsUseCase: ObserveFavoriteStationsUseCase,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val toggleFavoriteStationUseCase: ToggleFavoriteStationUseCase,
    private val navigator: Navigator,
    private val analyticsManager: AnalyticsTracking,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    initialState: FavoritesUIState = FavoritesUIState(),
) : ViewModel() {

    private val _state = MutableStateFlow(viewModelScope, initialState)

    @NativeCoroutinesState
    val state: StateFlow<FavoritesUIState> = _state.asStateFlow()

    init {
        launchStartupTasks(
            { analyticsManager.track(FavoritesScreenViewed) },
            { observeFavorites() },
        )
    }

    /**
     * Combined with the preferences so the prices shown, and the ordering, follow the user's default
     * fuel — and re-render when it changes on the settings screen.
     *
     * There is no distance column here: this screen has no location dependency, so it works
     * identically whether or not permission was ever granted.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun observeFavorites() {
        combine(observeFavoriteStationsUseCase(), observeUserPreferencesUseCase()) { result, preferences ->
            result to preferences.defaultFuelType.toFuelFilter()
        }.collect { (result, fuelFilter) ->
            val now = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE)
            val stations = withContext(defaultDispatcher) {
                result.stations
                    .map { station ->
                        station.toGasStationItemVO(
                            isOpen = station.isOpen(now),
                            distanceInKilometers = null,
                            fuelFilter = fuelFilter,
                        )
                    }
                    // Cheapest first: the whole point of keeping a favourites list is comparing them.
                    .sortedWith(compareBy(nullsLast()) { it.getCurrentFuelPrice() })
                    .markCheapest()
            }
            _state.update { it.withFavoritesLoaded(stations, result.unresolvedCount) }
        }
    }

    fun onItemClick(stationId: String) {
        viewModelScope.launch {
            analyticsManager.track(GasStationSelected(stationId))
            navigator.navigate(Destination.GasStationDetails(stationId))
        }
    }

    fun onToggleFavorite(stationId: String) {
        viewModelScope.launch { toggleFavoriteStationUseCase(stationId) }
    }

    fun onBackTapped() {
        viewModelScope.launch { navigator.navigateUp() }
    }

    /** Same rule as the list screen: only the first station at the cheapest price is marked. */
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
}
