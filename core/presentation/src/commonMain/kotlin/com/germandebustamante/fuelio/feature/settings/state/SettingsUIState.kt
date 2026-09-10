package com.germandebustamante.fuelio.feature.settings.state

import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode

// Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
data class SettingsUIState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultFuelType: FuelType = FuelType.GASOLINE_95,
    val isLoading: Boolean = true,
) {
    /**
     * No sealed `ContentState` here, unlike the list and detail screens: preferences are read from
     * local storage with no network path, so there is no empty state and no error state to model —
     * only "not read yet".
     */
    fun withPreferences(themeMode: ThemeMode, defaultFuelType: FuelType) =
        copy(themeMode = themeMode, defaultFuelType = defaultFuelType, isLoading = false)
}
