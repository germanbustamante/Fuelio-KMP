package com.germandebustamante.fuelio.core.featureflag

import app.cash.turbine.test
import com.germandebustamante.fuelio.core.analytics.featureflag.FeatureFlagSource
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsBy
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultFeatureFlagsTest {

    @Test
    fun `isEnabled - GIVEN the source knows a value THEN it is returned as-is`() {
        val source: FeatureFlagSource = mock {
            every { isEnabled("price_trend_chart", true) } returns false
        }

        val sut = DefaultFeatureFlags(source)

        assertFalse(sut.isEnabled("price_trend_chart", default = true))
    }

    @Test
    fun `observe - GIVEN flags not loaded yet THEN the current source value is emitted immediately`() = runTest {
        val source: FeatureFlagSource = mock {
            every { flagsLoaded() } returns MutableStateFlow(false)
            every { isEnabled("price_trend_chart", any()) } returns true
        }

        val sut = DefaultFeatureFlags(source)

        sut.observe("price_trend_chart", default = false).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe - GIVEN flagsLoaded flips to true after a refresh THEN the flag is re-checked`() = runTest {
        val flagsLoaded = MutableStateFlow(false)
        var isEnabled = false
        val source: FeatureFlagSource = mock {
            every { flagsLoaded() } returns flagsLoaded
            every { isEnabled("price_trend_chart", any()) } returnsBy { isEnabled }
        }

        val sut = DefaultFeatureFlags(source)

        sut.observe("price_trend_chart", default = false).test {
            assertFalse(awaitItem())

            isEnabled = true
            flagsLoaded.value = true

            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe - GIVEN an unknown key THEN the caller's default is returned`() = runTest {
        val source: FeatureFlagSource = mock {
            every { flagsLoaded() } returns MutableStateFlow(false)
            every { isEnabled("unknown_flag", true) } returns true
        }

        val sut = DefaultFeatureFlags(source)

        sut.observe("unknown_flag", default = true).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
