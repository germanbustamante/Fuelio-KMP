package com.germandebustamante.fuelio.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationItemVOs
import com.germandebustamante.fuelio.feature.list.ui.GasStationItem
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The list row, which is where the design tokens actually meet real data.
 *
 * Rendered from the same `fakeGasStationItemVOs` the previews and the iOS bridge tests use, so a
 * change to the VO shows up here as a visual diff rather than only as a failing assertion.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class GasStationItemScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rows() {
        val station = fakeGasStationItemVOs.first()

        composeRule.captureThemed("gas_station_item") {
            Column(
                modifier = Modifier.padding(FuelioSpacing.md),
                verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
            ) {
                GasStationItem(
                    gasStation = station,
                    isFavorite = false,
                    onItemClick = {},
                    onToggleFavorite = {},
                )
                // The cheapest badge and the filled star are the two states the plain row can't show.
                GasStationItem(
                    gasStation = station.copy(isCheapest = true),
                    isFavorite = true,
                    onItemClick = {},
                    onToggleFavorite = {},
                )
            }
        }
    }
}
