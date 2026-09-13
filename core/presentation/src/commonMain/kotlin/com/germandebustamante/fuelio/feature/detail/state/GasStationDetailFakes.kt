package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.fake.fakeGasStations
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

val fakePriceTrendVO = PriceTrendVO(
    points = listOf(
        PriceTrendPointVO(LocalDate(2026, 1, 1), 1.65),
        PriceTrendPointVO(LocalDate(2026, 1, 5), 1.70),
        PriceTrendPointVO(LocalDate(2026, 1, 10), 1.60),
        PriceTrendPointVO(LocalDate(2026, 1, 15), 1.72),
    ),
    minPrice = 1.60,
    maxPrice = 1.72,
)

val fakeGasStationDetailUIState = GasStationDetailUIState(
    gasStation = fakeGasStations[0],
    today = DayOfWeek.MONDAY,
    isLoading = false,
    priceTrend = fakePriceTrendVO,
)

val fakeGasStationDetailUIStateLoading = GasStationDetailUIState()

val fakeGasStationDetailUIStateNotFound = GasStationDetailUIState(
    gasStation = null,
    today = DayOfWeek.MONDAY,
    isLoading = false,
)
