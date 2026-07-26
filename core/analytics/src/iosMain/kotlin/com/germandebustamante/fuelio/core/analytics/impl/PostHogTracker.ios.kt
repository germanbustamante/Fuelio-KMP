package com.germandebustamante.fuelio.core.analytics.impl

import com.germandebustamante.fuelio.core.analytics.Trace
import com.germandebustamante.fuelio.core.analytics.di.AnalyticsContextProvider

actual fun getPostHogTracker(apiKey: String?, contextProvider: AnalyticsContextProvider): PostHogTracker? {
    if (apiKey.isNullOrBlank()) return null

    return IosPostHogTracker(requireNotNull(registeredNativeTracker) { "Call registerNativePostHogTracker() before getPostHogTracker()" })
}

private var registeredNativeTracker: NativePostHogTracker? = null

fun registerNativePostHogTracker(tracker: NativePostHogTracker) {
    registeredNativeTracker = tracker
}

interface NativePostHogTracker {
    fun logEvent(name: String, params: Map<String, Any>)
    fun logScreen(name: String, params: Map<String, Any>)
}

class IosPostHogTracker(private val nativeTracker: NativePostHogTracker) : PostHogTracker() {

    override fun onTrackEvent(trace: Trace.Event) {
        nativeTracker.logEvent(trace.eventName, trace.params.orEmpty())
    }

    override fun onTrackScreen(trace: Trace.Screen) {
        nativeTracker.logScreen(trace.eventName, trace.params.orEmpty())
    }
}
