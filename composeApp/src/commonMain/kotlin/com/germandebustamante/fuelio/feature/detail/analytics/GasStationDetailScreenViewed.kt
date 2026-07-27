package com.germandebustamante.fuelio.feature.detail.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data class GasStationDetailScreenViewed(val gasStationId: String) : Trace.Screen(
    screenName = SCREEN_NAME,
    targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
    params = mapOf(PARAM_GAS_STATION_ID to gasStationId),
) {
    companion object {
        const val SCREEN_NAME = "gas_station_detail"
        const val PARAM_GAS_STATION_ID = "gas_station_id"
    }
}
