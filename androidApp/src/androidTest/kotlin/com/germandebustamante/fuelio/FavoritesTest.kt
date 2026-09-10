package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import org.junit.Rule
import org.junit.Test

/** Android mirror of `iosAppUITests/FavoritesUITests.swift`. */
class FavoritesTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
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
    fun backReturnsToTheList() {
        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BUTTON).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_SCREEN).assertExists()

        composeRule.onNodeWithTag(A11yIdentifiers.FAVORITES_BACK_BUTTON).performClick()

        // Back goes through the ViewModel and the shared Navigator, not by popping the back stack.
        composeRule.onNodeWithTag(A11yIdentifiers.STATIONS_LIST).assertExists()
    }
}
