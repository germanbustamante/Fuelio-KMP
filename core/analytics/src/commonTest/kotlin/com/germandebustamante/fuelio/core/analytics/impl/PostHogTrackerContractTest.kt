package com.germandebustamante.fuelio.core.analytics.impl

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PostHogTrackerContractTest {

    private val sut = FakePostHogTracker()

    @Test
    fun `type - is always POSTHOG`() {
        assertEquals(AnalyticsProviderType.POSTHOG, sut.type)
    }

    @Test
    fun `track - GIVEN an Event trace WHEN track THEN only onTrackEvent is called`() = runTest {
        val trace = Trace.Event("purchase", targets = listOf(AnalyticsProviderType.POSTHOG))

        sut.track(trace)

        assertEquals(trace, sut.trackedEvent)
        assertNull(sut.trackedScreen)
    }

    @Test
    fun `track - GIVEN a Screen trace WHEN track THEN only onTrackScreen is called`() = runTest {
        val trace = Trace.Screen("gas_station_detail", targets = listOf(AnalyticsProviderType.POSTHOG))

        sut.track(trace)

        assertEquals(trace, sut.trackedScreen)
        assertNull(sut.trackedEvent)
    }

    private class FakePostHogTracker : PostHogTracker() {
        var trackedEvent: Trace.Event? = null
        var trackedScreen: Trace.Screen? = null

        override fun onTrackEvent(trace: Trace.Event) {
            trackedEvent = trace
        }

        override fun onTrackScreen(trace: Trace.Screen) {
            trackedScreen = trace
        }
    }
}
