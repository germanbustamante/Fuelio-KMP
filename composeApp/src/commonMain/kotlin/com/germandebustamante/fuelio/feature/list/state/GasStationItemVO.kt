package com.germandebustamante.fuelio.feature.list.state

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO

data class GasStationItemVO(
    val station: GasStationBO,
    val fuelFilter: FuelFilter = FuelFilter.Gasoline95,
    val isOpen: Boolean = false,
    val distanceInKilometers: Double? = null,
) {
    fun getCurrentFuelPrice(): Double? = when (fuelFilter) {
        is FuelFilter.Diesel -> station.dieselPrice
        is FuelFilter.DieselPremium -> station.dieselPremiumPrice
        is FuelFilter.Gasoline95 -> station.gasolinePrice95
        is FuelFilter.Gasoline98 -> station.gasolinePrice98
    }
}

fun GasStationBO.toGasStationItemVO(
    isOpen: Boolean,
    distanceInKilometers: Double?,
    fuelFilter: FuelFilter = FuelFilter.Gasoline95,
) = GasStationItemVO(
    station = this,
    fuelFilter = fuelFilter,
    isOpen = isOpen,
    distanceInKilometers = distanceInKilometers,
)

fun GasStationItemVO.withFuelFilter(filter: FuelFilter) = copy(fuelFilter = filter)

fun List<GasStationItemVO>.applySearchQuery(query: String): List<GasStationItemVO> {
    if (query.isBlank()) return this
    return filter { vo ->
        vo.station.name.contains(query, ignoreCase = true) ||
            vo.station.getFullDirection().contains(query, ignoreCase = true)
    }
}