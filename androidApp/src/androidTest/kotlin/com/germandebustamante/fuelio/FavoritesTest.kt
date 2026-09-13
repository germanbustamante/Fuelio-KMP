package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.testing.uiTestModule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.koin.core.context.GlobalContext

/**
 * Android mirror of `iosAppUITests/FavoritesUITests.swift`.
 *
 * `InMemoryFavoriteStationRepository` is a Koin `single`, so it survives across every test method
 * that shares this instrumentation process (unlike XCUITest, which relaunches the app per test) —
 * without a reset, a station starred by one test stays starred for the next, which flips these
 * tests' shared "click to star" assumption into "click to unstar" depending on run order.
 */
class FavoritesTest {

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
    fun favoritesStartsEmpty() {
        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BUTTON).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_SCREEN).assertExists()
        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_EMPTY).assertIsDisplayed()
    }

    @Test
    fun aStationStarredOnTheListShowsUpInFavorites() {
        // The whole point of the Room-backed favourites: the star survives leaving the list, because
        // the list's state is no longer the source of truth.
        composeRule.onNodeWithTag(A11yIdentifiers.favoriteButton(repsolStationId)).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BUTTON).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).assertExists()
    }

    @Test
    fun unstarringInFavoritesEmptiesTheScreen() {
        composeRule.onNodeWithTag(A11yIdentifiers.favoriteButton(repsolStationId)).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BUTTON).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).assertExists()

        composeRule.onNodeWithTag(A11yIdentifiers.favoriteButton(repsolStationId)).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_EMPTY).assertIsDisplayed()
    }

    @Test
    fun customAccessibilityActionRemovesAFavorite() {
        // TalkBack drives a row through its custom actions, not a nested testTag click — mirrors
        // `iosAppUITests/FavoritesUITests.swift` exercising the `.accessibilityAction(named:)`.
        composeRule.onNodeWithTag(A11yIdentifiers.favoriteButton(repsolStationId)).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BUTTON).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).assertExists()

        val node = composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).fetchSemanticsNode()
        val customActions = node.config.getOrNull(SemanticsActions.CustomActions).orEmpty()
        val removeAction = customActions.firstOrNull { it.label == "Remove from favorites" }
        assertNotNull("expected a 'Remove from favorites' custom accessibility action", removeAction)

        composeRule.runOnUiThread { removeAction?.action?.invoke() }

        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_EMPTY).assertIsDisplayed()
    }

    @Test
    fun backReturnsToTheList() {
        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BUTTON).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_SCREEN).assertExists()

        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BACK_BUTTON).performClick()

        // Back goes through the ViewModel and the shared Navigator, not by popping the back stack.
        composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
    }
}
