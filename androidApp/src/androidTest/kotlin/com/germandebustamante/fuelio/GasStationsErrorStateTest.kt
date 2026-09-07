package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.testing.uiTestModule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.koin.core.context.GlobalContext

/**
 * Android mirror of `GasStationsListUITests.testFailedLoadShowsTheErrorStateWithRetry` — isolated in
 * its own class because it needs the failing `uiTestModule` variant loaded *before* `MainActivity`
 * launches (the point where `GasStationsViewModel` first resolves its `GasStationRepository`), and
 * [ExternalResource.before] with `order = 0` is what guarantees that ordering against
 * `createAndroidComposeRule`'s own `order = 1` activity launch.
 */
class GasStationsErrorStateTest {

    @get:Rule(order = 0)
    val failingRepository = object : ExternalResource() {
        override fun before() {
            GlobalContext.get().loadModules(listOf(uiTestModule(simulateStationFailure = true)))
        }

        override fun after() {
            // Restore the passing graph so later test classes in this instrumentation process (JUnit
            // reuses one process for the whole run) see the normal fakes again.
            GlobalContext.get().loadModules(listOf(uiTestModule(simulateStationFailure = false)))
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
    fun failedLoadShowsTheErrorStateWithRetry() {
        composeRule.onNodeWithTag(A11yIdentifiers.ERROR_STATE).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.RETRY_BUTTON).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertDoesNotExist()
    }
}
