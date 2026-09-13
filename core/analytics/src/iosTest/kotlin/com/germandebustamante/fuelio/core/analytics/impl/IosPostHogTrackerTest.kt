package com.germandebustamante.fuelio.core.analytics.impl

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IosPostHogTrackerTest {

    private val nativeTracker = FakeNativePostHogTracker()
    private val sut = IosPostHogTracker(nativeTracker)

    @Test
    fun `onTrackEvent - GIVEN an Event trace THEN it is forwarded to the native tracker as-is`() = runTest {
        val trace = Trace.Event(
            eventName = "purchase",
            targets = listOf(AnalyticsProviderType.POSTHOG),
            params = mapOf("item" to "diesel"),
        )

        sut.track(trace)

        assertEquals("purchase", nativeTracker.loggedEventName)
        assertEquals(mapOf("item" to "diesel"), nativeTracker.loggedEventParams)
    }

    @Test
    fun `onTrackScreen - GIVEN a Screen trace THEN it is forwarded via logScreen with the screen name`() = runTest {
        val trace = Trace.Screen(
            screenName = "gas_station_detail",
            targets = listOf(AnalyticsProviderType.POSTHOG),
            params = mapOf("gas_station_id" to "station-1"),
        )

        sut.track(trace)

        assertEquals("gas_station_detail", nativeTracker.loggedScreenName)
        assertEquals(mapOf("gas_station_id" to "station-1"), nativeTracker.loggedScreenParams)
    }

    /**
     * Regression guard: `IosPostHogTracker` did not override `onTrackError` at all, so a
     * `Trace.Error` silently vanished on PostHog even though every `Trace` targets both providers.
     */
    @Test
    fun `onTrackError - GIVEN an Error trace THEN it is forwarded via logEvent with the error name`() = runTest {
        val trace = Trace.Error(
            eventName = "gas_stations_list_fetch_stations_failed",
            targets = listOf(AnalyticsProviderType.POSTHOG),
            params = mapOf("error_type" to "ServerError"),
        )

        sut.track(trace)

        assertEquals("gas_stations_list_fetch_stations_failed", nativeTracker.loggedEventName)
        assertEquals(mapOf("error_type" to "ServerError"), nativeTracker.loggedEventParams)
    }

    @Test
    fun `onIdentify - GIVEN a distinct id and properties THEN they are forwarded to the native tracker as-is`() = runTest {
        sut.identify("installation-1", mapOf("platform" to "ios"))

        assertEquals("installation-1", nativeTracker.identifiedDistinctId)
        assertEquals(mapOf("platform" to "ios"), nativeTracker.identifiedProperties)
    }

    private class FakeNativePostHogTracker : NativePostHogTracker {
        var loggedEventName: String? = null
        var loggedEventParams: Map<String, Any>? = null
        var loggedScreenName: String? = null
        var loggedScreenParams: Map<String, Any>? = null
        var identifiedDistinctId: String? = null
        var identifiedProperties: Map<String, Any>? = null

        override fun logEvent(name: String, params: Map<String, Any>) {
            loggedEventName = name
            loggedEventParams = params
        }

        override fun logScreen(name: String, params: Map<String, Any>) {
            loggedScreenName = name
            loggedScreenParams = params
        }

        override fun logIdentify(distinctId: String, properties: Map<String, Any>) {
            identifiedDistinctId = distinctId
            identifiedProperties = properties
        }
    }
}
