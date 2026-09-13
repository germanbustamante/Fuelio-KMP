package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import kotlinx.datetime.DayOfWeek

sealed interface ContentState {
    data object Loading : ContentState

    data class Success(val gasStation: GasStationBO, val today: DayOfWeek) : ContentState {
        val scheduleDays: List<ScheduleDayVO> = gasStation.schedule.toScheduleDays(today)
    }

    data object NotFound : ContentState
}

data class GasStationDetailUIState(
    val gasStation: GasStationBO? = null,
    val today: DayOfWeek? = null,
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    /** `null` while loading, or when the `price_trend_chart` flag is off — either way, the UI hides the chart. */
    val priceTrend: PriceTrendVO? = null,
    val isTrendLoading: Boolean = false,
) {
    val contentState: ContentState
        get() = when {
            isLoading -> ContentState.Loading
            gasStation != null && today != null -> ContentState.Success(gasStation, today)
            else -> ContentState.NotFound
        }

    fun withGasStationLoaded(gasStation: GasStationBO?, today: DayOfWeek) = copy(gasStation = gasStation, today = today, isLoading = false)

    fun withFavorite(isFavorite: Boolean) = copy(isFavorite = isFavorite)

    fun withPriceTrend(priceTrend: PriceTrendVO?) = copy(priceTrend = priceTrend, isTrendLoading = false)

    fun withTrendLoading() = copy(isTrendLoading = true)
}
