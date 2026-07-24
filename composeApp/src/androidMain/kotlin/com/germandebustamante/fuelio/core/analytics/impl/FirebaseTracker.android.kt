package com.germandebustamante.fuelio.core.analytics.impl

import com.germandebustamante.fuelio.core.analytics.Trace
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

actual fun getFirebaseTracker(): FirebaseTracker = AndroidFirebaseTracker()

class AndroidFirebaseTracker : FirebaseTracker() {

    override fun onTrackEvent(trace: Trace.Event) {
        Firebase.analytics.logEvent(trace.eventName) { trace.params?.forEach { (key, value) -> addParam(key, value) } }
    }

    override fun onTrackScreen(trace: Trace.Screen) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, trace.eventName)
            trace.params?.forEach { (key, value) -> addParam(key, value) }
        }
    }

    override fun onTrackError(trace: Trace.Error) {
        Firebase.analytics.logEvent(trace.eventName) { trace.params?.forEach { (key, value) -> addParam(key, value) } }
    }
}

private fun com.google.firebase.analytics.ParametersBuilder.addParam(key: String, value: Any) {
    when (value) {
        is String -> param(key, value)
        is Long -> param(key, value)
        is Int -> param(key, value.toLong())
        is Double -> param(key, value)
        else -> param(key, value.toString())
    }
}
