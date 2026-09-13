package com.germandebustamante.fuelio.core.analytics.featureflag

import android.content.Context
import com.germandebustamante.fuelio.core.analytics.di.AnalyticsContextProvider
import com.germandebustamante.fuelio.core.analytics.impl.PostHogAndroidSetup
import com.posthog.PostHog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual fun getFeatureFlagSource(apiKey: String?, contextProvider: AnalyticsContextProvider): FeatureFlagSource {
    if (apiKey.isNullOrBlank()) return NoOpFeatureFlagSource
    return AndroidPostHogFeatureFlagSource(contextProvider.getApplication(), apiKey)
}

class AndroidPostHogFeatureFlagSource(context: Context, apiKey: String) : FeatureFlagSource {

    init {
        PostHogAndroidSetup.ensure(context, apiKey)
    }

    private val loaded = MutableStateFlow(false)

    override suspend fun refresh() = suspendCancellableCoroutine { continuation ->
        PostHog.reloadFeatureFlags {
            loaded.value = true
            if (continuation.isActive) continuation.resume(Unit)
        }
    }

    override fun isEnabled(key: String, default: Boolean): Boolean = PostHog.isFeatureEnabled(key, defaultValue = default)

    override fun flagsLoaded(): Flow<Boolean> = loaded.asStateFlow()
}
