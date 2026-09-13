package com.germandebustamante.fuelio.feature.common.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

/** Fired for every external `fuelio://` URI the app receives, resolved or not — a bad/unsupported link is a no-op for navigation but still worth knowing about. */
data class DeepLinkOpened(val uri: String, val resolved: Boolean) :
    Trace.Event(
        eventName = EVENT_NAME,
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
        params = mapOf(PARAM_URI to uri, PARAM_RESOLVED to resolved),
    ) {
    companion object {
        const val EVENT_NAME = "app_deep_link_opened"
        const val PARAM_URI = "uri"
        const val PARAM_RESOLVED = "resolved"
    }
}
