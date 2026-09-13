package com.germandebustamante.fuelio.feature.app.state

import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode

/**
 * [hasCompletedOnboarding] is `null` until the first preferences read resolves, rather than
 * defaulting to `false` — a `false` default would flash the onboarding screen for every user on
 * every cold start until that read lands, even for someone who finished onboarding weeks ago. The
 * app shell renders nothing (just the themed background) while it is `null`.
 */
// Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
data class AppUIState(val themeMode: ThemeMode = ThemeMode.SYSTEM, val hasCompletedOnboarding: Boolean? = null)
