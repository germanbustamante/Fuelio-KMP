package com.germandebustamante.fuelio.feature.map.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data object MapScreenViewed : Trace.Screen(
    screenName = "gas_stations_map",
    targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
) {
    const val SCREEN_NAME = "gas_stations_map"
}
