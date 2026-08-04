package com.germandebustamante.fuelio.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.designtokens.FuelioTypeTokens

internal val FuelioFontFamily = FontFamily(
    Font(R.font.inter_variable, weight = FontWeight.Thin, style = FontStyle.Normal),
    Font(R.font.inter_variable, weight = FontWeight.ExtraLight, style = FontStyle.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Light, style = FontStyle.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(R.font.inter_variable, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(R.font.inter_variable, weight = FontWeight.ExtraBold, style = FontStyle.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Black, style = FontStyle.Normal),
    Font(R.font.inter_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.inter_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.inter_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
)

internal val FuelioTypography: Typography
    get() {
        val fontFamily = FuelioFontFamily
        return Typography(
            displayLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = FuelioTypeTokens.DISPLAY_LARGE_SIZE.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp,
            ),
            displayMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = FuelioTypeTokens.DISPLAY_MEDIUM_SIZE.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp,
            ),
            displaySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = FuelioTypeTokens.DISPLAY_SMALL_SIZE.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp,
            ),
            headlineLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = FuelioTypeTokens.HEADLINE_LARGE_SIZE.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = FuelioTypeTokens.HEADLINE_MEDIUM_SIZE.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = FuelioTypeTokens.HEADLINE_SMALL_SIZE.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = FuelioTypeTokens.TITLE_LARGE_SIZE.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = FuelioTypeTokens.TITLE_MEDIUM_SIZE.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
            ),
            titleSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = FuelioTypeTokens.TITLE_SMALL_SIZE.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = FuelioTypeTokens.BODY_LARGE_SIZE.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = FuelioTypeTokens.BODY_MEDIUM_SIZE.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = FuelioTypeTokens.BODY_SMALL_SIZE.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = FuelioTypeTokens.LABEL_LARGE_SIZE.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = FuelioTypeTokens.LABEL_MEDIUM_SIZE.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = FuelioTypeTokens.LABEL_SMALL_SIZE.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        )
    }
