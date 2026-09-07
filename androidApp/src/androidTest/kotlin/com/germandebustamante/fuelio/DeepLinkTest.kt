package com.germandebustamante.fuelio

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.net.toUri
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import org.junit.Rule
import org.junit.Test

/**
 * Android mirror of `iosAppUITests/DeepLinkUITests.swift`, exercised through the real
 * `android.intent.action.VIEW` intent the manifest declares — not a shortcut, so it also proves the
 * manifest wiring (`AndroidManifest.xml`'s `fuelio://` intent-filter), `MainActivity`'s
 * `ExternalUriHandler.onNewUri` forwarding, and `FuelioNavHost`'s synthetic-back-stack listener all
 * work end to end.
 *
 * Uses `createEmptyComposeRule()` plus a manually launched [ActivityScenario] rather than
 * `createAndroidComposeRule<MainActivity>()`, since that rule only supports the activity's default
 * launch intent and these tests need a custom one.
 */
class DeepLinkTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private val repsolStationId = "7153"

    /** Absent from the fakes — `getGasStationById` is a local-only lookup, so this is deterministic. */
    private val uncachedStationId = "000000"

    private fun deepLinkIntent(uri: String): Intent {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return Intent(Intent.ACTION_VIEW, uri.toUri(), context, MainActivity::class.java)
    }

    @Test
    fun deepLinkOpensTheStationDetail() {
        ActivityScenario.launch<MainActivity>(deepLinkIntent("fuelio://station/$repsolStationId/detail")).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_STATION_NAME).assertExists()
        }
    }

    @Test
    fun backFromADeepLinkLandsOnTheStationList() {
        ActivityScenario.launch<MainActivity>(deepLinkIntent("fuelio://station/$repsolStationId/detail")).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_BACK_BUTTON).performClick()

            // The synthetic back stack is what makes this the list rather than exiting the app.
            composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
        }
    }

    @Test
    fun deepLinkToAnUncachedStationShowsNotFound() {
        ActivityScenario.launch<MainActivity>(deepLinkIntent("fuelio://station/$uncachedStationId/detail")).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_NOT_FOUND).assertExists()
        }
    }

    @Test
    fun unsupportedDeepLinkStaysOnTheList() {
        ActivityScenario.launch<MainActivity>(deepLinkIntent("fuelio://station/$repsolStationId/map")).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
            composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_STATION_NAME).assertDoesNotExist()
        }
    }

    /** Regression test for the sticky-intent bug `MainActivity.consumeDeepLinkIfNeeded` fixes. */
    @Test
    fun rotatingAfterADeepLinkDoesNotReapplyIt() {
        ActivityScenario.launch<MainActivity>(deepLinkIntent("fuelio://station/$repsolStationId/detail")).use { scenario ->
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_BACK_BUTTON).performClick()
            composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()

            // `recreate()` re-delivers the same sticky launch Intent to the new Activity instance,
            // the same as an actual configuration change (e.g. a rotation) would.
            scenario.recreate()
            composeRule.waitForIdle()

            // Without the `deepLinkConsumed` guard this would reapply the deep link and jump back to
            // the detail screen out from under the user.
            composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
            composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_STATION_NAME).assertDoesNotExist()
        }
    }
}
