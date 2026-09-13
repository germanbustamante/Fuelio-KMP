package com.germandebustamante.fuelio.feature.list.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

/** Shared by the list and Favorites screens — both toggle the same favourite, just from different rows. */
data class FavoriteToggled(val gasStationId: String, val isFavorite: Boolean) :
    Trace.Event(
        eventName = EVENT_NAME,
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
        params = mapOf(PARAM_GAS_STATION_ID to gasStationId, PARAM_IS_FAVORITE to isFavorite),
    ) {
    companion object {
        const val EVENT_NAME = "gas_stations_list_favorite_toggled"
        const val PARAM_GAS_STATION_ID = "gas_station_id"
        const val PARAM_IS_FAVORITE = "is_favorite"
    }
}
