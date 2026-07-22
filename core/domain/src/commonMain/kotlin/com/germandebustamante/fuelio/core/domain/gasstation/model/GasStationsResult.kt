package com.germandebustamante.fuelio.core.domain.gasstation.model

data class GasStationsResult(
    val stations: List<GasStationBO>,
    val isFromCache: Boolean,
)
