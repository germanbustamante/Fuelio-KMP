package com.germandebustamante.fuelio.feature.list.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data object SettingsOpened : Trace.Event(
    eventName = "gas_stations_list_settings_opened",
    targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
) {
    const val EVENT_NAME = "gas_stations_list_settings_opened"
}
