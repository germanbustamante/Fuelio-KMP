package com.germandebustamante.fuelio.core.domain.preferences.model

/**
 * Which colour scheme the app should use.
 *
 * [SYSTEM] means "whatever the OS says" and is the default; the other two override it. Resolving
 * [SYSTEM] into an actual light/dark answer is the one part that has to happen per platform, since
 * only the UI layer knows the current system appearance.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
