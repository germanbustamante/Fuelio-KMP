package com.germandebustamante.fuelio.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.chip.FuelioFilterChip

@Composable
fun ThemeModeSelector(selectedThemeMode: ThemeMode, onThemeModeSelected: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag(A11yIdentifiers.THEME_PICKER)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
    ) {
        ThemeMode.entries.forEach { themeMode ->
            FuelioFilterChip(
                selected = themeMode == selectedThemeMode,
                onClick = { onThemeModeSelected(themeMode) },
                label = themeMode.label(),
                modifier = Modifier.testTag(A11yIdentifiers.themeOption(themeMode.rawValue)),
            )
        }
    }
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

/** Derived from the enum name so Android and iOS can't drift apart on the identifier. */
private val ThemeMode.rawValue: String get() = name.lowercase()

@Preview(showBackground = true)
@Composable
private fun ThemeModeSelectorPreview() {
    FuelioTheme {
        ThemeModeSelector(selectedThemeMode = ThemeMode.DARK, onThemeModeSelected = {})
    }
}
