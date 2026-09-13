package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.testing.uiTestModule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.koin.core.context.GlobalContext

/**
 * Android mirror of `iosAppUITests/GasStationDetailUITests.swift`.
 *
 * Shares `FavoritesTest`'s reset rule: `starringFromTheDetailScreenShowsUpInFavorites` toggles the
 * same Koin-`single` favourites fake, so it needs the same fresh-state guarantee regardless of what
 * ran before it in this instrumentation process.
 */
class GasStationDetailTest {

    @get:Rule(order = 0)
    val freshFakes = object : ExternalResource() {
        override fun before() {
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

    private val repsolStationId = "7153"

    @Test
    fun detailShowsPricesScheduleAndDirections() {
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_STATION_NAME).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_PRICES_SECTION).assertExists()
        // The schedule section is the last item in the detail's LazyColumn. On a short enough
        // device/emulator it falls outside the initially-composed window entirely, so it is not a
        // "hidden but present" node `performScrollTo()` (on the item's own tag) can find — a lazy
        // item that hasn't been composed yet isn't in the semantics tree at all. Scrolling has to be
        // driven from the container via `performScrollToNode`, which knows how to walk a lazy list by
        // index until the target appears.
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_CONTENT_LIST)
            .performScrollToNode(hasTestTag(A11yIdentifiers.DETAIL_SCHEDULE_SECTION))
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

    @Test
    fun starringFromTheDetailScreenShowsUpInFavorites() {
        // Same source of truth as the list's star — the favourites table, not screen-local state —
        // so a toggle from the detail screen must be visible from the Favorites screen too.
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_FAVORITE_BUTTON).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_BACK_BUTTON).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BUTTON).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).assertExists()
    }
}
