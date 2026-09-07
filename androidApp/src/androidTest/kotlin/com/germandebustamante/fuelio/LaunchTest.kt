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

    // Location permission is granted up front so `GasStationsViewModel.initLocationPermission()`
    // (which runs automatically on launch) never blocks the test behind a real system dialog —
    // there is no Koin-resolvable fake for `LocationPermissionController` on Android the way there
    // is on iOS, since MainActivity constructs `AndroidLocationPermissionController` directly.
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
