package com.germandebustamante.fuelio.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ========== PALETTE COLORS ==========

// Primary - Orange (Energy/Fuel)
internal val Orange500 = Color(0xFFFF6B00)
internal val Orange400 = Color(0xFFFF8534)
internal val Orange300 = Color(0xFFFFA366)
internal val Orange600 = Color(0xFFE65100)
internal val Orange700 = Color(0xFFCC4700)
internal val Orange50 = Color(0xFFFFF3E0)
internal val Orange100 = Color(0xFFFFE0B2)

// Secondary - Blue (Trust/Technology)
internal val Blue500 = Color(0xFF2196F3)
internal val Blue400 = Color(0xFF42A5F5)
internal val Blue300 = Color(0xFF64B5F6)
internal val Blue600 = Color(0xFF1976D2)
internal val Blue700 = Color(0xFF1565C0)
internal val Blue50 = Color(0xFFE3F2FD)
internal val Blue100 = Color(0xFFBBDEFB)

// Tertiary - Green (Savings/Eco)
internal val Green500 = Color(0xFF4CAF50)
internal val Green400 = Color(0xFF66BB6A)
internal val Green300 = Color(0xFF81C784)
internal val Green600 = Color(0xFF43A047)
internal val Green700 = Color(0xFF388E3C)
internal val Green50 = Color(0xFFE8F5E9)
internal val Green100 = Color(0xFFC8E6C9)

// Error - Red
internal val Red500 = Color(0xFFF44336)
internal val Red400 = Color(0xFFEF5350)
internal val Red300 = Color(0xFFE57373)
internal val Red600 = Color(0xFFE53935)
internal val Red700 = Color(0xFFD32F2F)
internal val Red50 = Color(0xFFFFEBEE)
internal val Red100 = Color(0xFFFFCDD2)

// Neutrals - Gray
internal val Gray50 = Color(0xFFFAFAFA)
internal val Gray100 = Color(0xFFF5F5F5)
internal val Gray200 = Color(0xFFEEEEEE)
internal val Gray300 = Color(0xFFE0E0E0)
internal val Gray400 = Color(0xFFBDBDBD)
internal val Gray500 = Color(0xFF9E9E9E)
internal val Gray600 = Color(0xFF757575)
internal val Gray700 = Color(0xFF616161)
internal val Gray800 = Color(0xFF424242)
internal val Gray900 = Color(0xFF212121)

// ========== LIGHT COLOR SCHEME ==========

internal val LightColorScheme = lightColorScheme(
    // Primary
    primary = Orange500,
    onPrimary = Color.White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange700,

    // Secondary
    secondary = Blue500,
    onSecondary = Color.White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue700,

    // Tertiary
    tertiary = Green500,
    onTertiary = Color.White,
    tertiaryContainer = Green100,
    onTertiaryContainer = Green700,

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
    inversePrimary = Orange300,

    // Other
    scrim = Color.Black.copy(alpha = 0.32f),
    surfaceTint = Orange500,
)

// ========== DARK COLOR SCHEME ==========

internal val DarkColorScheme = darkColorScheme(
    // Primary
    primary = Orange400,
    onPrimary = Orange700,
    primaryContainer = Orange600,
    onPrimaryContainer = Orange100,

    // Secondary
    secondary = Blue400,
    onSecondary = Blue700,
    secondaryContainer = Blue600,
    onSecondaryContainer = Blue100,

    // Tertiary
    tertiary = Green400,
    onTertiary = Green700,
    tertiaryContainer = Green600,
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
    surface = Gray900,
    onSurface = Gray100,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,

    // Outline
    outline = Gray600,
    outlineVariant = Gray700,

    // Inverse
    inverseSurface = Gray100,
    inverseOnSurface = Gray900,
    inversePrimary = Orange600,

    // Other
    scrim = Color.Black.copy(alpha = 0.32f),
    surfaceTint = Orange400,
)
