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
) = GasStationItemVO(
    station = this,
    isOpen = isOpen,
    distanceInKilometers = distanceInKilometers,
)