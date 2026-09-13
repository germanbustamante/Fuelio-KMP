package com.germandebustamante.fuelio.feature.onboarding.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import com.germandebustamante.fuelio.feature.onboarding.state.OnboardingStep

/** [step] is where the user bailed out — the other half of the funnel [OnboardingStepViewed] gives. */
data class OnboardingSkipped(val step: OnboardingStep) :
    Trace.Event(
        eventName = EVENT_NAME,
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
        params = mapOf(PARAM_STEP to step.name.lowercase()),
    ) {
    companion object {
        const val EVENT_NAME = "onboarding_skipped"
        const val PARAM_STEP = "step"
    }
}
