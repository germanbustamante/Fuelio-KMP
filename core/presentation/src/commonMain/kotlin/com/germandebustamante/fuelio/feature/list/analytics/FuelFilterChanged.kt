package com.germandebustamante.fuelio.feature.list.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data class FuelFilterChanged(val fuelFilter: String) :
    Trace.Event(
        eventName = EVENT_NAME,
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
        params = mapOf(PARAM_FUEL_FILTER to fuelFilter),
    ) {
    companion object {
        const val EVENT_NAME = "gas_stations_list_fuel_filter_changed"
        const val PARAM_FUEL_FILTER = "fuel_filter"
    }
}
