package com.germandebustamante.fuelio.feature.settings.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType

data class DefaultFuelChanged(val fuelType: FuelType) :
    Trace.Event(
        eventName = EVENT_NAME,
        params = mapOf(PARAM_FUEL_TYPE to fuelType.name.lowercase()),
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
    ) {
    companion object {
        const val EVENT_NAME = "settings_default_fuel_changed"
        const val PARAM_FUEL_TYPE = "fuel_type"
    }
}
