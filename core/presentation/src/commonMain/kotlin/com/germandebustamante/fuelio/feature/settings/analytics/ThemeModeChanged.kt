package com.germandebustamante.fuelio.feature.settings.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode

data class ThemeModeChanged(val themeMode: ThemeMode) :
    Trace.Event(
        eventName = EVENT_NAME,
        params = mapOf(PARAM_THEME_MODE to themeMode.name.lowercase()),
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
    ) {
    companion object {
        const val EVENT_NAME = "settings_theme_mode_changed"
        const val PARAM_THEME_MODE = "theme_mode"
    }
}
