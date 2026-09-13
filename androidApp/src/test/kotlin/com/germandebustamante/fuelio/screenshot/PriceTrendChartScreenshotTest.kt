package com.germandebustamante.fuelio.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.feature.detail.state.PriceTrendPointVO
import com.germandebustamante.fuelio.feature.detail.state.PriceTrendVO
import com.germandebustamante.fuelio.feature.detail.ui.PriceTrendChart
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class PriceTrendChartScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun chart() {
        val trend = PriceTrendVO(
            points = listOf(
                PriceTrendPointVO(LocalDate(2026, 1, 1), 1.65),
                PriceTrendPointVO(LocalDate(2026, 1, 5), 1.70),
                PriceTrendPointVO(LocalDate(2026, 1, 10), 1.60),
                PriceTrendPointVO(LocalDate(2026, 1, 15), 1.72),
            ),
            minPrice = 1.60,
            maxPrice = 1.72,
        )

        composeRule.captureThemed("price_trend_chart") {
            PriceTrendChart(trend = trend, modifier = Modifier.padding(FuelioSpacing.md))
        }
    }
}
