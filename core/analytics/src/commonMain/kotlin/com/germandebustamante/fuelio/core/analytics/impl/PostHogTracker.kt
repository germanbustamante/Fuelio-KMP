package com.germandebustamante.fuelio.core.analytics.impl

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Tracker
import com.germandebustamante.fuelio.core.analytics.di.AnalyticsContextProvider

expect fun getPostHogTracker(apiKey: String?, contextProvider: AnalyticsContextProvider): PostHogTracker?

abstract class PostHogTracker : Tracker() {
    final override val type: AnalyticsProviderType = AnalyticsProviderType.POSTHOG
}
