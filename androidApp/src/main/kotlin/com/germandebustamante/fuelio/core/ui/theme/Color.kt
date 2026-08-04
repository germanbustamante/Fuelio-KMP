package com.germandebustamante.fuelio.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.germandebustamante.fuelio.core.designtokens.FuelioColorTokens

// ========== PALETTE COLORS ==========
// Values shared with iOS (iosApp/DesignSystem/FuelioColors.swift) come from FuelioColorTokens in
// :core:presentation — the single source of truth. Android-only ramp steps (Material containers,
// inverse roles…) stay as local literals here.

// Primary - Deep Orange (Energy/Fuel)
internal val DeepOrange50 = Color(0xFFFBE9E7)
internal val DeepOrange100 = Color(0xFFFFCCBC)
internal val DeepOrange200 = Color(0xFFFFAB91)
internal val DeepOrange300 = Color(FuelioColorTokens.DEEP_ORANGE_300)
internal val DeepOrange400 = Color(0xFFFF7043)
internal val DeepOrange500 = Color(0xFFFF5722)
internal val DeepOrange600 = Color(FuelioColorTokens.DEEP_ORANGE_600)
internal val DeepOrange700 = Color(0xFFE64A19)
internal val DeepOrange800 = Color(0xFFD84315)
internal val DeepOrange900 = Color(0xFFBF360C)
internal val DeepOrange950 = Color(0xFF8A2607)

// Secondary - Blue Grey (Trust/Technology)
internal val BlueGrey50 = Color(0xFFECEFF1)
internal val BlueGrey100 = Color(0xFFCFD8DC)
internal val BlueGrey200 = Color(0xFFB0BEC5)
internal val BlueGrey300 = Color(0xFF90A4AE)
internal val BlueGrey400 = Color(0xFF78909C)
internal val BlueGrey500 = Color(0xFF607D8B)
internal val BlueGrey600 = Color(0xFF546E7A)
internal val BlueGrey700 = Color(0xFF455A64)
internal val BlueGrey800 = Color(0xFF37474F)
internal val BlueGrey900 = Color(0xFF263238)

// Tertiary - Green (Savings/Eco)
internal val Green500 = Color(FuelioColorTokens.GREEN_500)
internal val Green400 = Color(FuelioColorTokens.GREEN_400)
internal val Green300 = Color(0xFF81C784)
internal val Green600 = Color(0xFF43A047)
internal val Green700 = Color(0xFF388E3C)
internal val Green50 = Color(0xFFE8F5E9)
internal val Green100 = Color(0xFFC8E6C9)
internal val Green950 = Color(0xFF1B4620)

// Error - Red
internal val Red500 = Color(FuelioColorTokens.RED_500)
internal val Red400 = Color(FuelioColorTokens.RED_400)
internal val Red300 = Color(0xFFE57373)
internal val Red600 = Color(0xFFE53935)
internal val Red700 = Color(0xFFD32F2F)
internal val Red50 = Color(0xFFFFEBEE)
internal val Red100 = Color(0xFFFFCDD2)

// Neutrals - Gray
internal val Gray50 = Color(FuelioColorTokens.GRAY_50)
internal val Gray100 = Color(0xFFF5F5F5)
internal val Gray200 = Color(0xFFEEEEEE)
internal val Gray300 = Color(0xFFE0E0E0)
internal val Gray400 = Color(FuelioColorTokens.GRAY_400)
internal val Gray500 = Color(0xFF9E9E9E)
internal val Gray600 = Color(FuelioColorTokens.GRAY_600)
internal val Gray700 = Color(0xFF616161)
internal val Gray800 = Color(FuelioColorTokens.GRAY_800)
internal val Gray900 = Color(FuelioColorTokens.GRAY_900)

// ========== LIGHT COLOR SCHEME ==========

internal val LightColorScheme = lightColorScheme(
    // Primary
    primary = DeepOrange600,
    onPrimary = Color.White,
    primaryContainer = DeepOrange100,
    onPrimaryContainer = DeepOrange950,

    // Secondary
    secondary = BlueGrey600,
    onSecondary = Color.White,
    secondaryContainer = BlueGrey100,
    onSecondaryContainer = BlueGrey900,

    // Tertiary
    tertiary = Green500,
    onTertiary = Color.White,
    tertiaryContainer = Green100,
    onTertiaryContainer = Green950,

    // Error
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red700,

    // Background
    background = Gray50,
    onBackground = Gray900,

    // Surface
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray200,
    onSurfaceVariant = Gray700,

    // Outline
    outline = Gray400,
    outlineVariant = Gray300,

    // Inverse
    inverseSurface = Gray800,
    inverseOnSurface = Gray100,
    inversePrimary = DeepOrange300,

    // Other
    scrim = Color.Black.copy(alpha = 0.32f),
    surfaceTint = DeepOrange600,
)

// ========== DARK COLOR SCHEME ==========

internal val DarkColorScheme = darkColorScheme(
    // Primary
    primary = DeepOrange300,
    onPrimary = DeepOrange900,
    primaryContainer = DeepOrange950,
    onPrimaryContainer = DeepOrange100,

    // Secondary
    secondary = BlueGrey300,
    onSecondary = BlueGrey900,
    secondaryContainer = BlueGrey700,
    onSecondaryContainer = BlueGrey100,

    // Tertiary
    tertiary = Green400,
    onTertiary = Green700,
    tertiaryContainer = Green950,
    onTertiaryContainer = Green100,

    // Error
    error = Red400,
    onError = Red700,
    errorContainer = Red600,
    onErrorContainer = Red100,

    // Background
    background = Gray900,
    onBackground = Gray100,

    // Surface
    surface = Gray800,
    onSurface = Gray100,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray300,

    // Outline
    outline = Gray600,
    outlineVariant = Gray700,

    // Inverse
    inverseSurface = Gray100,
    inverseOnSurface = Gray900,
    inversePrimary = DeepOrange700,

    // Other
    scrim = Color.Black.copy(alpha = 0.32f),
    surfaceTint = DeepOrange300,
)
