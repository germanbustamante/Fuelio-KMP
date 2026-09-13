package com.germandebustamante.fuelio.feature.map.state

import com.germandebustamante.fuelio.core.fake.fakeGasStations

val fakeMapUIState = MapUIState(
    markers = fakeGasStations.map { it.toMapMarkerVO() },
    isLoading = false,
)
