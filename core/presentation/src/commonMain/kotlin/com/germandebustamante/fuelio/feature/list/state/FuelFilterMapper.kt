package com.germandebustamante.fuelio.feature.list.state

import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType

/**
 * Bridges the presentation-layer [FuelFilter] and the domain-level [FuelType].
 *
 * They are deliberately separate types rather than one shared enum: `:core:domain` can't depend on
 * presentation, and `FuelFilter` is a sealed interface because the UI switches on it exhaustively.
 * Both `when`s here are exhaustive with no `else`, so adding a fuel is a compile error in this file
 * rather than a silently unmapped value.
 */
fun FuelFilter.toFuelType(): FuelType = when (this) {
    FuelFilter.Gasoline95 -> FuelType.GASOLINE_95
    FuelFilter.Gasoline98 -> FuelType.GASOLINE_98
    FuelFilter.Diesel -> FuelType.DIESEL
    FuelFilter.DieselPremium -> FuelType.DIESEL_PREMIUM
}

fun FuelType.toFuelFilter(): FuelFilter = when (this) {
    FuelType.GASOLINE_95 -> FuelFilter.Gasoline95
    FuelType.GASOLINE_98 -> FuelFilter.Gasoline98
    FuelType.DIESEL -> FuelFilter.Diesel
    FuelType.DIESEL_PREMIUM -> FuelFilter.DieselPremium
}
