package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import org.junit.Rule
import org.junit.Test

/**
 * Android mirror of `iosAppUITests/LaunchUITests.swift`. Runs against [FuelioTestApplication]'s
 * deterministic Koin graph (installed by [FuelioTestRunner]), so it exercises the same fakes the
 * XCUITest suite does — never the network or Room.
 */
class LaunchTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // Belt and braces: `uiTestModule`'s `LocationPermissionController` fake never touches the real
    // Android permission APIs, so no system dialog can appear regardless of this rule — but granting
    // it up front keeps this suite correct even if a test is ever pointed at the production Koin
    // graph by mistake.
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    /** First entry of `core/fake/FakeGasStations.kt`. */
    private val repsolStationId = "7153"

    @Test
    fun appLaunchesAndShowsTheStationList() {
        composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
    }

    @Test
    fun tappingAStationOpensItsDetail() {
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_STATION_NAME).assertExists()
    }
}
