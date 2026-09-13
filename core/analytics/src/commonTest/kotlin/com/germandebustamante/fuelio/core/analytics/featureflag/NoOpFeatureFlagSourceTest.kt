package com.germandebustamante.fuelio.core.analytics.featureflag

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoOpFeatureFlagSourceTest {

    @Test
    fun `isEnabled - GIVEN any key WHEN checked THEN the caller's default is returned`() {
        assertTrue(NoOpFeatureFlagSource.isEnabled("any_flag", default = true))
        assertFalse(NoOpFeatureFlagSource.isEnabled("any_flag", default = false))
    }

    @Test
    fun `flagsLoaded - GIVEN refresh is called THEN it never flips to true`() = runTest {
        NoOpFeatureFlagSource.refresh()

        assertFalse(NoOpFeatureFlagSource.flagsLoaded().first())
    }
}
