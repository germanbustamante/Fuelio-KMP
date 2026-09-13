package com.germandebustamante.fuelio

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag

/**
 * Compose's own automatic synchronization (every `onNodeWith*`/`performClick` waits until idle)
 * only tracks recomposition and animations. It does not track a state change that arrives through a
 * ViewModel's `viewModelScope` coroutine round trip — a preferences write, a repository call, a Flow
 * `collect` — since that dispatch can be scheduled after the idle-check's own bounded retry loop has
 * already given up. Any assertion that depends on such a round trip having finished should poll for
 * its target tag instead of assuming a single `waitForIdle()`/interaction sync already covers it.
 */
fun ComposeTestRule.waitUntilTagExists(tag: String, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis = timeoutMillis) { onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
}
