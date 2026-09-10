package com.germandebustamante.fuelio.feature.app.state

import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode

// Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
data class AppUIState(val themeMode: ThemeMode = ThemeMode.SYSTEM)
