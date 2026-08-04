import SwiftUI
import CorePresentation

/// Semantic colors shared with Android via `FuelioColorTokens` (`:core:presentation`).
/// Only brand + roles that make sense on iOS are mirrored here — Material-only roles
/// (`primaryContainer`, `onSurfaceVariant`, `surfaceTint`, `inversePrimary`…) are intentionally
/// NOT ported; use system colors/materials for those (`.secondary`, `.regularMaterial`,
/// `Color(.systemGroupedBackground)`).
enum FuelioColors {
    static let accent = Color.dynamic(
        light: FuelioColorTokens.shared.ACCENT_LIGHT,
        dark: FuelioColorTokens.shared.ACCENT_DARK
    )

    static let success = Color.dynamic(
        light: FuelioColorTokens.shared.SUCCESS_LIGHT,
        dark: FuelioColorTokens.shared.SUCCESS_DARK
    )

    static let danger = Color.dynamic(
        light: FuelioColorTokens.shared.DANGER_LIGHT,
        dark: FuelioColorTokens.shared.DANGER_DARK
    )

    static let background = Color.dynamic(
        light: FuelioColorTokens.shared.BACKGROUND_LIGHT,
        dark: FuelioColorTokens.shared.BACKGROUND_DARK
    )

    static let surface = Color.dynamic(
        light: FuelioColorTokens.shared.SURFACE_LIGHT,
        dark: FuelioColorTokens.shared.SURFACE_DARK
    )

    static let onSurface = Color.dynamic(
        light: FuelioColorTokens.shared.ON_SURFACE_LIGHT,
        dark: FuelioColorTokens.shared.ON_SURFACE_DARK
    )

    static let outline = Color.dynamic(
        light: FuelioColorTokens.shared.OUTLINE_LIGHT,
        dark: FuelioColorTokens.shared.OUTLINE_DARK
    )
}
