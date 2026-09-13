package com.germandebustamante.fuelio.core.analytics.featureflag

import com.germandebustamante.fuelio.core.analytics.di.AnalyticsContextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual fun getFeatureFlagSource(apiKey: String?, contextProvider: AnalyticsContextProvider): FeatureFlagSource {
    if (apiKey.isNullOrBlank()) return NoOpFeatureFlagSource
    return IosFeatureFlagSource(
        requireNotNull(registeredNativeFeatureFlagSource) {
            "Call registerNativeFeatureFlagSource() before getFeatureFlagSource()"
        },
    )
}

private var registeredNativeFeatureFlagSource: NativeFeatureFlagSource? = null

fun registerNativeFeatureFlagSource(source: NativeFeatureFlagSource) {
    registeredNativeFeatureFlagSource = source
}

interface NativeFeatureFlagSource {
    /** Callback-based, not `suspend`, so Swift implements this as a plain method rather than the
     * completion-handler override a Kotlin `suspend` interface member would force on it. */
    fun reload(onLoaded: () -> Unit)
    fun isFeatureEnabled(key: String, default: Boolean): Boolean
}

class IosFeatureFlagSource(private val nativeSource: NativeFeatureFlagSource) : FeatureFlagSource {

    private val loaded = MutableStateFlow(false)

    override suspend fun refresh() = suspendCancellableCoroutine { continuation ->
        nativeSource.reload {
            loaded.value = true
            if (continuation.isActive) continuation.resume(Unit)
        }
    }

    override fun isEnabled(key: String, default: Boolean): Boolean = nativeSource.isFeatureEnabled(key, default)

    override fun flagsLoaded(): Flow<Boolean> = loaded.asStateFlow()
}
