package com.germandebustamante.fuelio.core.navigation.destination

import kotlinx.serialization.Serializable

// NavKey (androidx.navigation3) vive ahora en :androidApp — ver DestinationNavKey.
@Serializable
sealed interface Destination {

    @Serializable
    data object GasStations: Destination

    @Serializable
    data class GasStationDetails(val gasStationId: String): Destination
}