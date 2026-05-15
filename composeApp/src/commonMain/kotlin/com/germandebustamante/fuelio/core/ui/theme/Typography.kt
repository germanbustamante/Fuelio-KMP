package com.germandebustamante.fuelio.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.inter_italic
import fuelio.composeapp.generated.resources.inter_variable
import org.jetbrains.compose.resources.Font

internal val FuelioFontFamily
    @androidx.compose.runtime.Composable
    get() = FontFamily(
        Font(Res.font.inter_variable, weight = FontWeight.Thin, style = FontStyle.Normal),
        Font(Res.font.inter_variable, weight = FontWeight.ExtraLight, style = FontStyle.Normal),
        Font(Res.font.inter_variable, weight = FontWeight.Light, style = FontStyle.Normal),
        Font(Res.font.inter_variable, weight = FontWeight.Normal, style = FontStyle.Normal),
        Font(Res.font.inter_variable, weight = FontWeight.Medium, style = FontStyle.Normal),
        Font(Res.font.inter_variable, weight = FontWeight.SemiBold, style = FontStyle.Normal),
        Font(Res.font.inter_variable, weight = FontWeight.Bold, style = FontStyle.Normal),
        Font(Res.font.inter_variable, weight = FontWeight.ExtraBold, style = FontStyle.Normal),
        Font(Res.font.inter_variable, weight = FontWeight.Black, style = FontStyle.Normal),
        Font(Res.font.inter_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(Res.font.inter_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
        Font(Res.font.inter_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    )

internal val FuelioTypography
    @androidx.compose.runtime.Composable
    get(): Typography {
        val fontFamily = FuelioFontFamily
        return Typography(
            displayLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp,
            ),
            displayMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp,
            ),
            displaySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp,
            ),
            headlineLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
            ),
            titleSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        )
    }
