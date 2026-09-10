package com.germandebustamante.fuelio.feature.settings.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

// A `data object` can't reference its own companion constant from inside super(...) ("Cannot access
// before initialized"), so the literal is inlined there and duplicated in the public const.
data object SettingsScreenViewed : Trace.Screen(
    screenName = "settings",
    targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
) {
    const val SCREEN_NAME = "settings"
}
