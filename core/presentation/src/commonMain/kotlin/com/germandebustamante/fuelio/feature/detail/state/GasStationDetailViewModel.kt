package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ObserveFavoriteStationIdsUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ObservePriceHistoryUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ToggleFavoriteStationUseCase
import com.germandebustamante.fuelio.core.domain.util.SPAIN_TIMEZONE
import com.germandebustamante.fuelio.core.featureflag.FeatureFlags
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.common.viewmodel.launchStartupTasks
import com.germandebustamante.fuelio.feature.detail.analytics.DirectionsRequested
import com.germandebustamante.fuelio.feature.detail.analytics.GasStationDetailScreenViewed
import com.germandebustamante.fuelio.feature.list.analytics.FavoriteToggled
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GasStationDetailViewModel(
    private val route: Destination.GasStationDetails,
    private val getGasStation: GetGasStationUseCase,
    private val navigator: Navigator,
    private val analyticsManager: AnalyticsTracking,
    private val observeFavoriteStationIdsUseCase: ObserveFavoriteStationIdsUseCase,
    private val toggleFavoriteStationUseCase: ToggleFavoriteStationUseCase,
    private val observePriceHistoryUseCase: ObservePriceHistoryUseCase,
    private val featureFlags: FeatureFlags,
    initialState: GasStationDetailUIState = GasStationDetailUIState(),
) : ViewModel() {

    // MutableStateFlow(viewModelScope, …) is the KMP-ObservableViewModel overload: it is what
    // notifies SwiftUI on every emission. A plain kotlinx MutableStateFlow would still work on
    // Android but would never repaint the iOS views.
    private val _state: MutableStateFlow<GasStationDetailUIState> = MutableStateFlow(viewModelScope, initialState)

    @NativeCoroutinesState
    val state: StateFlow<GasStationDetailUIState> = _state.asStateFlow()

    init {
        launchStartupTasks(
            { analyticsManager.track(GasStationDetailScreenViewed(route.gasStationId)) },
            {
                getGasStation(route.gasStationId).collect { gasStation ->
                    val today = Clock.System.now().toLocalDateTime(SPAIN_TIMEZONE).dayOfWeek
                    _state.update { it.withGasStationLoaded(gasStation, today) }
                }
            },
            {
                // The favourites table is the source of truth, same as the list screen — this star
                // reflects what is stored, not what was last tapped.
                observeFavoriteStationIdsUseCase().collect { favoriteIds ->
                    _state.update { it.withFavorite(route.gasStationId in favoriteIds) }
                }
            },
            { observePriceTrend() },
        )
    }

    /**
     * Re-subscribes whenever the flag flips (`flatMapLatest`), so a toggle in the PostHog dashboard
     * shows or hides the chart on an already-open screen without needing a relaunch.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observePriceTrend() {
        _state.update { it.withTrendLoading() }
        featureFlags.observe(PRICE_TREND_CHART_FLAG_KEY, default = false)
            .flatMapLatest { enabled ->
                if (enabled) observePriceHistoryUseCase(route.gasStationId).map { it.toPriceTrendVO() } else flowOf(null)
            }
            .collectLatest { priceTrend ->
                _state.update { it.withPriceTrend(priceTrend) }
            }
    }

    fun onBackClick() {
        viewModelScope.launch { navigator.navigateUp() }
    }

    fun onDirectionsTapped() {
        viewModelScope.launch { analyticsManager.track(DirectionsRequested(route.gasStationId)) }
    }

    fun onToggleFavorite() {
        val willBeFavorite = !_state.value.isFavorite
        viewModelScope.launch {
            toggleFavoriteStationUseCase(route.gasStationId)
            analyticsManager.track(FavoriteToggled(route.gasStationId, willBeFavorite))
        }
    }

    companion object {
        const val PRICE_TREND_CHART_FLAG_KEY = "price_trend_chart"
    }
}
