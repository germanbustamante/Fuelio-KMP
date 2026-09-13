package com.germandebustamante.fuelio.core.analytics.impl

import android.content.Context
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

internal const val POSTHOG_HOST = "https://eu.i.posthog.com"

/**
 * Guards `PostHogAndroid.setup` behind a single process-wide call.
 *
 * Both [AndroidPostHogTracker] and the Android [com.germandebustamante.fuelio.core.analytics.featureflag.getFeatureFlagSource]
 * actual need the SDK set up, and Koin resolves `single {}` lazily on first use — whichever one a
 * caller reaches first would otherwise call `PostHogAndroid.setup` a second time, which
 * reinitializes the SDK's session/identity state rather than being a no-op.
 */
internal object PostHogAndroidSetup {
    private var isSetUp = false

    @Synchronized
    fun ensure(context: Context, apiKey: String) {
        if (isSetUp) return
        PostHogAndroid.setup(context, PostHogAndroidConfig(apiKey = apiKey, host = POSTHOG_HOST))
        isSetUp = true
    }
}
