package com.germandebustamante.fuelio.core.analytics

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TrackerTest {

    private val sut = FakeTracker()

    @Test
    fun `track - GIVEN an Event trace WHEN track THEN only onTrackEvent is called`() = runTest {
        val trace = Trace.Event("purchase", targets = listOf(AnalyticsProviderType.FIREBASE))

        sut.track(trace)

        assertEquals(trace, sut.trackedEvent)
        assertNull(sut.trackedScreen)
        assertNull(sut.trackedError)
    }

    @Test
    fun `track - GIVEN a Screen trace WHEN track THEN only onTrackScreen is called`() = runTest {
        val trace = Trace.Screen("gas_station_detail", targets = listOf(AnalyticsProviderType.FIREBASE))

        sut.track(trace)

        assertEquals(trace, sut.trackedScreen)
        assertNull(sut.trackedEvent)
        assertNull(sut.trackedError)
    }

    @Test
    fun `track - GIVEN an Error trace WHEN track THEN only onTrackError is called`() = runTest {
        val trace = Trace.Error("network_failure", targets = listOf(AnalyticsProviderType.FIREBASE))

        sut.track(trace)

        assertEquals(trace, sut.trackedError)
        assertNull(sut.trackedEvent)
        assertNull(sut.trackedScreen)
    }

    private class FakeTracker : Tracker() {
        override val type = AnalyticsProviderType.FIREBASE

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
