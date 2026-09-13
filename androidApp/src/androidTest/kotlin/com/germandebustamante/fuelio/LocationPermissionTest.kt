package com.germandebustamante.fuelio

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import org.junit.Rule
import org.junit.Test

/**
 * Android mirror of `iosAppUITests/LocationPermissionUITests.swift`.
 *
 * Closes the parity gap left by P1's instrumentation suite: `uiTestModule`'s
 * `LocationPermissionController` fake reports a permanent denial without touching any real Android
 * permission API, so the "denied for good" branch is deterministic and no system dialog can block
 * the run — see `PresentationPlatformModule.android.kt` and `LocationPermissionControllerHolder` for
 * how that fake now actually reaches `GasStationsViewModel` (previously `MainActivity` always built
 * the real, Activity-bound controller directly, so this Koin override existed but was never used).
 */
class LocationPermissionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun detectingLocationWhenPermanentlyDeniedOffersSettings() {
        composeRule.onNodeWithTag(A11yIdentifiers.DETECT_LOCATION_BUTTON).performClick()

        composeRule.onNodeWithText(SETTINGS_ACTION_LABEL).assertExists()
    }

    @Test
    fun dismissingThePermissionSnackbarLetsItReappearOnTheNextAttempt() {
        composeRule.onNodeWithTag(A11yIdentifiers.DETECT_LOCATION_BUTTON).performClick()
        composeRule.onNodeWithText(SETTINGS_ACTION_LABEL).assertExists()

        // Compose's Snackbar auto-dismisses after its duration; waiting it out exercises the same
        // `onDismissPermissionSnackbar()` state reset the iOS alert's dismiss button exercises.
        // `SnackbarDuration.Long` is ~10s, so a 10s timeout races it exactly at the boundary — give
        // it real margin instead of matching the duration 1:1.
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(SETTINGS_ACTION_LABEL).fetchSemanticsNodes().isEmpty()
        }

        composeRule.onNodeWithTag(A11yIdentifiers.DETECT_LOCATION_BUTTON).performClick()
        composeRule.onNodeWithText(SETTINGS_ACTION_LABEL).assertExists()
    }

    private companion object {
        const val SETTINGS_ACTION_LABEL = "Open Settings"
    }
}
