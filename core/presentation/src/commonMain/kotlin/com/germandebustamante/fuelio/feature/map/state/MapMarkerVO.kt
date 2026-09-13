package com.germandebustamante.fuelio.feature.map.state

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO

// Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
data class MapMarkerVO(
    val gasStationId: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val cheapestPrice: Double?,
)

fun GasStationBO.toMapMarkerVO() = MapMarkerVO(
    gasStationId = id,
    displayName = displayName,
    latitude = latitude,
    longitude = longitude,
    cheapestPrice = listOfNotNull(gasolinePrice95, gasolinePrice98, dieselPrice, dieselPremiumPrice).minOrNull(),
)
