package com.germandebustamante.fuelio.feature.list.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

enum class LocationPermissionOutcome { REQUESTED, GRANTED, DENIED, DENIED_PERMANENTLY }

data class LocationPermissionEvent(val outcome: LocationPermissionOutcome) :
    Trace.Event(
        eventName = "${GasStationsScreenViewed.SCREEN_NAME}_location_permission_${outcome.name.lowercase()}",
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
    )
