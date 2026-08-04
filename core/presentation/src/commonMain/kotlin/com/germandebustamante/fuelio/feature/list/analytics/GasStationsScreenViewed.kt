package com.germandebustamante.fuelio.feature.list.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data object GasStationsScreenViewed : Trace.Screen(
    screenName = "gas_stations_list",
    targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
) {
    const val SCREEN_NAME = "gas_stations_list"
}
