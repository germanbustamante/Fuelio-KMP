package com.germandebustamante.fuelio.core.navigation.destination

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination: NavKey {

    @Serializable
    data object GasStations: Destination

    @Serializable
    data class GasStationDetails(val gasStationId: String): Destination
}