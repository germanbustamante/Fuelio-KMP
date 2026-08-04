package com.germandebustamante.fuelio.core.designtokens

/**
 * Shared base size (sp/pt) and weight (CSS-style 100-900 scale) per Material type role.
 * Source of truth for `androidApp/core/ui/theme/Typography.kt`.
 *
 * iOS does NOT replicate the full 15-role fixed scale: `iosApp/DesignSystem/FuelioTypography.swift`
 * maps a handful of these onto iOS's semantic Dynamic Type styles (`.title3`, `.body`…), using the
 * size as a Dynamic-Type-relative base. Line height and letter spacing are Android-only — on iOS
 * that's Dynamic Type's job, not this module's.
 */
object FuelioTypeTokens {
    const val DISPLAY_LARGE_SIZE = 57.0
    const val DISPLAY_LARGE_WEIGHT = 400

    const val DISPLAY_MEDIUM_SIZE = 45.0
    const val DISPLAY_MEDIUM_WEIGHT = 400

    const val DISPLAY_SMALL_SIZE = 36.0
    const val DISPLAY_SMALL_WEIGHT = 400

    const val HEADLINE_LARGE_SIZE = 32.0
    const val HEADLINE_LARGE_WEIGHT = 600

    const val HEADLINE_MEDIUM_SIZE = 28.0
    const val HEADLINE_MEDIUM_WEIGHT = 600

    const val HEADLINE_SMALL_SIZE = 24.0
    const val HEADLINE_SMALL_WEIGHT = 600

    const val TITLE_LARGE_SIZE = 22.0
    const val TITLE_LARGE_WEIGHT = 600

    const val TITLE_MEDIUM_SIZE = 16.0
    const val TITLE_MEDIUM_WEIGHT = 500

    const val TITLE_SMALL_SIZE = 14.0
    const val TITLE_SMALL_WEIGHT = 500

    const val BODY_LARGE_SIZE = 16.0
    const val BODY_LARGE_WEIGHT = 400

    const val BODY_MEDIUM_SIZE = 14.0
    const val BODY_MEDIUM_WEIGHT = 400

    const val BODY_SMALL_SIZE = 12.0
    const val BODY_SMALL_WEIGHT = 400

    const val LABEL_LARGE_SIZE = 14.0
    const val LABEL_LARGE_WEIGHT = 500

    const val LABEL_MEDIUM_SIZE = 12.0
    const val LABEL_MEDIUM_WEIGHT = 500

    const val LABEL_SMALL_SIZE = 11.0
    const val LABEL_SMALL_WEIGHT = 500
}
