package com.germandebustamante.fuelio.core.analytics.impl

import com.germandebustamante.fuelio.core.analytics.Trace

actual fun getFirebaseTracker(): FirebaseTracker =
    IosFirebaseTracker(requireNotNull(registeredNativeTracker) { "Call registerNativeFirebaseTracker() before getFirebaseTracker()" })

private var registeredNativeTracker: NativeFirebaseTracker? = null

fun registerNativeFirebaseTracker(tracker: NativeFirebaseTracker) {
    registeredNativeTracker = tracker
}

interface NativeFirebaseTracker {
    fun logEvent(name: String, params: Map<String, Any>)
}

class IosFirebaseTracker(private val nativeTracker: NativeFirebaseTracker) : FirebaseTracker() {

    override fun onTrackEvent(trace: Trace.Event) {
        nativeTracker.logEvent(trace.eventName, trace.params.orEmpty())
    }

    override fun onTrackScreen(trace: Trace.Screen) {
        nativeTracker.logEvent("screen_view", mapOf("screen_name" to trace.eventName) + trace.params.orEmpty())
    }

    override fun onTrackError(trace: Trace.Error) {
        nativeTracker.logEvent(trace.eventName, trace.params.orEmpty())
    }
}
