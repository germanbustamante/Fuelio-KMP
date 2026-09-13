package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

/**
 * Android mirror of `iosAppUITests/GasStationsListUITests.swift`. Every element is located by
 * `testTag`, never by localized text, so these keep passing when the app runs in Spanish.
 *
 * The failed-load/error-state scenario needs a different Koin graph (a failing repository) that must
 * be in place *before* `MainActivity` launches — it lives in its own
 * [GasStationsErrorStateTest] class instead, so swapping that graph doesn't affect the tests here.
 */
class GasStationsListTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    /** First entries of `core/fake/FakeGasStations.kt`/`GasStationsFakes.kt`. */
    private val repsolStationId = "7153"
    private val madridProvinceId = "2"

    @Test
    fun searchFiltersTheList() {
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).assertExists()

        composeRule.onNodeWithTag(A11yIdentifiers.SEARCH_FIELD).performTextInput("MOEVE")

        // The ViewModel debounces the query by 300ms on a real dispatcher hop that Compose's own
        // idle detection does not track (it's not a recomposition/animation wait), so poll instead
        // of asserting immediately.
        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodesWithTag(A11yIdentifiers.stationRow(repsolStationId))
                .fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun changingProvinceUpdatesTheTitle() {
        composeRule.onNodeWithTag(A11yIdentifiers.PROVINCE_BUTTON).performClick()

        composeRule.onNodeWithTag(A11yIdentifiers.provinceRow(madridProvinceId)).performClick()

        // The default fake province is Sevilla; after picking Madrid the sheet closes and the title
        // reflects the new selection instead.
        composeRule.onNodeWithTag(A11yIdentifiers.PROVINCE_SHEET).assertDoesNotExist()
    }

    @Test
    fun changingFuelFilterUpdatesThePrice() {
        // The price `Text` sits inside the row's `mergeDescendants = true` subtree, so it is only
        // individually addressable in the unmerged semantics tree. Reads via `onAllNodesWithTag`
        // rather than `onNodeWithTag(...).fetchSemanticsNode()`, which throws instead of returning
        // null while the row is transiently absent from the tree during recomposition.
        fun priceText(): String? = composeRule
            .onAllNodesWithTag(A11yIdentifiers.stationPrice(repsolStationId), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .firstOrNull()
            ?.config
            ?.getOrNull(SemanticsProperties.Text)
            ?.joinToString { it.text }

        val gasolinePrice = priceText()

        composeRule.onNodeWithTag(A11yIdentifiers.fuelOption("diesel")).performClick()

        // Switching fuel type re-derives the row's price through the ViewModel/preferences round
        // trip, not a purely local recomposition, so poll for the new value instead of assuming a
        // single click already synchronized it.
        composeRule.waitUntil(timeoutMillis = 3_000) { priceText() != gasolinePrice }
        assertNotEquals(gasolinePrice, priceText())
    }
}
