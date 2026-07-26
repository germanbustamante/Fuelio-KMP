package com.germandebustamante.fuelio.core.analytics.impl

import android.content.Context
import com.germandebustamante.fuelio.core.analytics.Trace
import com.germandebustamante.fuelio.core.analytics.di.AnalyticsContextProvider
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

private const val POSTHOG_HOST = "https://eu.i.posthog.com"

actual fun getPostHogTracker(apiKey: String?, contextProvider: AnalyticsContextProvider): PostHogTracker? {
    if (apiKey.isNullOrBlank()) return null
    return AndroidPostHogTracker(contextProvider.getApplication(), apiKey)
}

class AndroidPostHogTracker(context: Context, apiKey: String) : PostHogTracker() {

    init {
        PostHogAndroid.setup(
            context,
            PostHogAndroidConfig(apiKey = apiKey, host = POSTHOG_HOST),
        )
    }

    override fun onTrackEvent(trace: Trace.Event) {
        PostHog.capture(event = trace.eventName, properties = trace.params)
    }

    override fun onTrackScreen(trace: Trace.Screen) {
        PostHog.screen(screenTitle = trace.eventName, properties = trace.params)
    }
}
