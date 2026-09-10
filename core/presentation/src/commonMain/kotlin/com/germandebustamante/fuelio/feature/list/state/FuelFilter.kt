package com.germandebustamante.fuelio.feature.list.state

sealed interface FuelFilter {
    data object Gasoline95 : FuelFilter
    data object Gasoline98 : FuelFilter
    data object Diesel : FuelFilter
    data object DieselPremium : FuelFilter
}
