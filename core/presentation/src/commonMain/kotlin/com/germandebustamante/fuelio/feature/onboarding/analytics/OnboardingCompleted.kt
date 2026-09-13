package com.germandebustamante.fuelio.feature.onboarding.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data object OnboardingCompleted : Trace.Event(
    eventName = "onboarding_completed",
    targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
) {
    const val EVENT_NAME = "onboarding_completed"
}
