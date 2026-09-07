package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import org.junit.Rule
import org.junit.Test

/** Android mirror of `iosAppUITests/GasStationDetailUITests.swift`. */
class GasStationDetailTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private val repsolStationId = "7153"

    @Test
    fun detailShowsPricesScheduleAndDirections() {
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_STATION_NAME).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_PRICES_SECTION).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_SCHEDULE_SECTION).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_DIRECTIONS_BUTTON).assertExists()
    }

    @Test
    fun backReturnsToTheList() {
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_STATION_NAME).assertExists()

        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_BACK_BUTTON).performClick()

        // Back goes through the ViewModel and the shared Navigator, not by popping the back stack
        // directly, so this also proves that round trip works — same intent as the iOS test.
        composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
    }

    // The detail "not found" state cannot be reached by tapping through the list — it only offers
    // stations that are already cached — but a deep link to an uncached station id reaches exactly
    // that state; see `DeepLinkTest.deepLinkToAnUncachedStationShowsNotFound`.
}
