package com.germandebustamante.fuelio.core.navigation.destination

import kotlinx.serialization.Serializable

// NavKey (androidx.navigation3) vive ahora en :androidApp — ver DestinationNavKey.
//
// Every direct subtype of this interface becomes a `case` of the Swift enum SKIE generates
// (`onEnum(of:)`), so only concrete screens belong here: a marker sub-interface like "is
// deep-linkable" would add a `case` carrying a protocol type Swift can't narrow, and would also
// shadow the concrete `case`s in the generated `onEnum` — see SyntheticBackStack.kt.
@Serializable
sealed interface Destination {

    @Serializable
    data object GasStations : Destination

    @Serializable
    data class GasStationDetails(val gasStationId: String) : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data object Favorites : Destination
}
