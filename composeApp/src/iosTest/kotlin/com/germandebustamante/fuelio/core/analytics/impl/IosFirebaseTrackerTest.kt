package com.germandebustamante.fuelio.core.analytics.impl

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import kotlin.test.Test
import kotlin.test.assertEquals

class IosFirebaseTrackerTest {

    private val nativeTracker = FakeNativeFirebaseTracker()
    private val sut = IosFirebaseTracker(nativeTracker)

    @Test
    fun `onTrackEvent - GIVEN an Event trace THEN it is forwarded to the native tracker as-is`() {
        val trace = Trace.Event(
            eventName = "purchase",
            targets = listOf(AnalyticsProviderType.FIREBASE),
            params = mapOf("item" to "diesel"),
        )

        sut.track(trace)

        assertEquals("purchase", nativeTracker.loggedName)
        assertEquals(mapOf("item" to "diesel"), nativeTracker.loggedParams)
    }

    @Test
    fun `onTrackScreen - GIVEN a Screen trace THEN it is forwarded as a screen_view event with screen_name`() {
        val trace = Trace.Screen(
            screenName = "gas_station_detail",
            targets = listOf(AnalyticsProviderType.FIREBASE),
            params = mapOf("gas_station_id" to "station-1"),
        )

        sut.track(trace)

        assertEquals("screen_view", nativeTracker.loggedName)
        assertEquals(
            mapOf("screen_name" to "gas_station_detail", "gas_station_id" to "station-1"),
            nativeTracker.loggedParams,
        )
    }

    @Test
    fun `onTrackError - GIVEN an Error trace THEN it is forwarded to the native tracker as-is`() {
        val trace = Trace.Error(
            eventName = "network_failure",
            targets = listOf(AnalyticsProviderType.FIREBASE),
            params = mapOf("code" to "500"),
        )

        sut.track(trace)

        assertEquals("network_failure", nativeTracker.loggedName)
        assertEquals(mapOf("code" to "500"), nativeTracker.loggedParams)
    }

    private class FakeNativeFirebaseTracker : NativeFirebaseTracker {
        var loggedName: String? = null
        var loggedParams: Map<String, Any>? = null

        override fun logEvent(name: String, params: Map<String, Any>) {
            loggedName = name
            loggedParams = params
        }
    }
}
