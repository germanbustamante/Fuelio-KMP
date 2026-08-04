package com.germandebustamante.fuelio.core.designtokens

/**
 * Single source of truth for Fuelio's brand colors. ARGB `Long` values so both Compose
 * (`androidApp/core/ui/theme/Color.kt`) and SwiftUI (`iosApp/DesignSystem/FuelioColors.swift`)
 * read the exact same numbers — no duplication, no drift.
 *
 * Only the ramps/roles that are actually shared cross-platform live here. Android additionally
 * derives the full Material `ColorScheme` (`primaryContainer`, `surfaceTint`, `inversePrimary`…)
 * from these same ramps — those roles are Material-specific and stay Android-only.
 */
object FuelioColorTokens {

    // Raw ramps (subset used by the shared roles below)
    const val DEEP_ORANGE_600 = 0xFFF4511EL
    const val DEEP_ORANGE_300 = 0xFFFF8A65L
    const val GREEN_500 = 0xFF4CAF50L
    const val GREEN_400 = 0xFF66BB6AL
    const val RED_500 = 0xFFF44336L
    const val RED_400 = 0xFFEF5350L
    const val GRAY_50 = 0xFFFAFAFAL
    const val GRAY_400 = 0xFFBDBDBDL
    const val GRAY_600 = 0xFF757575L
    const val GRAY_800 = 0xFF424242L
    const val GRAY_900 = 0xFF212121L
    const val WHITE = 0xFFFFFFFFL

    // Semantic roles — light/dark pairs
    const val ACCENT_LIGHT = DEEP_ORANGE_600
    const val ACCENT_DARK = DEEP_ORANGE_300

    const val SUCCESS_LIGHT = GREEN_500
    const val SUCCESS_DARK = GREEN_400

    const val DANGER_LIGHT = RED_500
    const val DANGER_DARK = RED_400

    const val BACKGROUND_LIGHT = GRAY_50
    const val BACKGROUND_DARK = GRAY_900

    const val SURFACE_LIGHT = WHITE
    const val SURFACE_DARK = GRAY_800

    const val ON_SURFACE_LIGHT = GRAY_900
    const val ON_SURFACE_DARK = GRAY_50

    const val OUTLINE_LIGHT = GRAY_400
    const val OUTLINE_DARK = GRAY_600
}
