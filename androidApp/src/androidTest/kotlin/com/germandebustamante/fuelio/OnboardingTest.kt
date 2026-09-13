package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.testing.uiTestModule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.koin.core.context.GlobalContext

/**
 * Android mirror of `iosAppUITests/OnboardingUITests.swift` — isolated in its own class for the same
 * reason as [GasStationsErrorStateTest]: the `hasCompletedOnboarding = false` graph has to be loaded
 * before `MainActivity` launches, since that's when `AppViewModel` first observes preferences.
 */
class OnboardingTest {

    @get:Rule(order = 0)
    val onboardingNotCompleted = object : ExternalResource() {
        override fun before() {
            GlobalContext.get().loadModules(listOf(uiTestModule(simulateStationFailure = false, hasCompletedOnboarding = false)))
        }

        override fun after() {
            GlobalContext.get().loadModules(listOf(uiTestModule(simulateStationFailure = false, hasCompletedOnboarding = true)))
        }
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    @Test
    fun appLaunchesIntoOnboardingWhenNotCompletedYet() {
        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_SCREEN).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertDoesNotExist()
    }

    @Test
    fun finishingOnboardingRevealsTheStationList() {
        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_NEXT_BUTTON).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_ALLOW_LOCATION_BUTTON).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_FINISH_BUTTON).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_SCREEN).assertDoesNotExist()
    }

    @Test
    fun skippingFromTheWelcomeStepRevealsTheStationList() {
        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_SKIP_BUTTON).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
    }

    @Test
    fun skippingLocationPermissionStillReachesTheDefaultFuelStep() {
        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_NEXT_BUTTON).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_SKIP_BUTTON).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.ONBOARDING_FUEL_PICKER).assertExists()
    }
}
