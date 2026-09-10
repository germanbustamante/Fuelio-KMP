package com.germandebustamante.fuelio.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode

@Composable
fun FuelioTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FuelioTypography,
        shapes = FuelioShapes,
        content = content,
    )
}

/**
 * Resolves the stored preference into an actual light/dark answer.
 *
 * [ThemeMode.SYSTEM] is the only case that needs the platform, which is why this lives here and not
 * in `:core:presentation`: only the UI layer knows the current system appearance. iOS answers the
 * same question by mapping the mode onto a `ColorScheme?` and letting SwiftUI resolve `nil`.
 *
 * [FuelioTheme] keeps its `darkTheme: Boolean = isSystemInDarkTheme()` default, so the ~20 existing
 * previews go on compiling unchanged.
 */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
