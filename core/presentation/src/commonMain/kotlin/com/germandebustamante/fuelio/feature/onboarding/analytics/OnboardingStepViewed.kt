package com.germandebustamante.fuelio.feature.onboarding.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import com.germandebustamante.fuelio.feature.onboarding.state.OnboardingStep

/** One screen view per step — this is what gives the funnel its drop-off points. */
data class OnboardingStepViewed(val step: OnboardingStep) :
    Trace.Screen(
        screenName = "onboarding_${step.name.lowercase()}",
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
    )
