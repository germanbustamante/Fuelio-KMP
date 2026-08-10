package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.fake.fakeGasStations
import kotlinx.datetime.DayOfWeek

val fakeGasStationDetailUIState = GasStationDetailUIState(
    gasStation = fakeGasStations[0],
    today = DayOfWeek.MONDAY,
    isLoading = false,
)

val fakeGasStationDetailUIStateLoading = GasStationDetailUIState()

val fakeGasStationDetailUIStateNotFound = GasStationDetailUIState(
    gasStation = null,
    today = DayOfWeek.MONDAY,
    isLoading = false,
)
