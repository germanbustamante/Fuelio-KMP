package com.germandebustamante.fuelio.feature.map.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data class MapMarkerSelected(val gasStationId: String) :
    Trace.Event(
        eventName = EVENT_NAME,
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
        params = mapOf(PARAM_GAS_STATION_ID to gasStationId),
    ) {
    companion object {
        const val EVENT_NAME = "gas_stations_map_station_selected"
        const val PARAM_GAS_STATION_ID = "gas_station_id"
    }
}
