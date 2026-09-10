package com.germandebustamante.fuelio.feature.favorites.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data object FavoritesScreenViewed : Trace.Screen(
    screenName = "favorites",
    targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
) {
    const val SCREEN_NAME = "favorites"
}
