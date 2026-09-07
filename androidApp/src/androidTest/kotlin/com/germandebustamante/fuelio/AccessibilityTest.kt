package com.germandebustamante.fuelio

import android.Manifest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Android mirror of `iosAppUITests/AccessibilityUITests.swift`: every clickable element must carry a
 * non-blank content description or text label, and a station row's merged description reads as one
 * sentence rather than several disconnected fragments.
 */
class AccessibilityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private val repsolStationId = "7153"

    private fun assertEveryClickableIsLabelled() {
        val clickableNodes = composeRule.onAllNodes(hasClickAction(), useUnmergedTree = false)
            .fetchSemanticsNodes()
        assertTrue("expected at least one clickable element", clickableNodes.isNotEmpty())

        clickableNodes.forEach { node ->
            val contentDescription = node.config.getOrNull(SemanticsProperties.ContentDescription)
                ?.joinToString { it }
            val text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }
            val label = (contentDescription ?: text).orEmpty().trim()

            assertTrue("a clickable element has no label: $node", label.isNotEmpty())
        }
    }

    @Test
    fun everyInteractiveElementOnTheListIsLabelled() {
        assertEveryClickableIsLabelled()
    }

    @Test
    fun everyInteractiveElementOnTheDetailIsLabelled() {
        composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId)).performClick()
        composeRule.onNodeWithTag(A11yIdentifiers.DETAIL_STATION_NAME).assertExists()

        assertEveryClickableIsLabelled()
    }

    @Test
    fun stationRowsReadAsASingleSentence() {
        val node = composeRule.onNodeWithTag(A11yIdentifiers.stationRow(repsolStationId))
            .fetchSemanticsNode()
        val label = node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString { it }.orEmpty()

        // A composed sentence, not several separate announcements, and not truncated.
        assertTrue("row label should not be blank", label.isNotBlank())
        assertTrue("row label should read as one sentence (comma-joined fragments)", label.contains(","))
        assertTrue("row label looks truncated", label.length > 30)
    }
}
