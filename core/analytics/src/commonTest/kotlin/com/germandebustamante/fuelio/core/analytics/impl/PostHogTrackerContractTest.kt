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

    /**
     * Regression guard: `PostHogTracker.android.kt`/`.ios.kt` did not override `onTrackError` at
     * all, so every `Trace.Error` (e.g. `ApiCallFailed`) silently vanished on PostHog despite every
     * `Trace` declaring both `FIREBASE` and `POSTHOG` as targets — errors only ever reached Firebase.
     */
    @Test
    fun `track - GIVEN an Error trace WHEN track THEN onTrackError is called`() = runTest {
        val trace = Trace.Error("fetch_stations_failed", targets = listOf(AnalyticsProviderType.POSTHOG))

        sut.track(trace)

        assertEquals(trace, sut.trackedError)
        assertNull(sut.trackedEvent)
        assertNull(sut.trackedScreen)
    }

    private class FakePostHogTracker : PostHogTracker() {
        var trackedEvent: Trace.Event? = null
        var trackedScreen: Trace.Screen? = null
        var trackedError: Trace.Error? = null

        override fun onTrackEvent(trace: Trace.Event) {
            trackedEvent = trace
        }

        override fun onTrackScreen(trace: Trace.Screen) {
            trackedScreen = trace
        }

        override fun onTrackError(trace: Trace.Error) {
            trackedError = trace
        }
    }
}
