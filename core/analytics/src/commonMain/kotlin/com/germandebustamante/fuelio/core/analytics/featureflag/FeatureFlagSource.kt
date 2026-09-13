package com.germandebustamante.fuelio.core.analytics.featureflag

import com.germandebustamante.fuelio.core.analytics.di.AnalyticsContextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SDK-agnostic feature flag lookup. `:core:presentation`'s facade (`FeatureFlags`/
 * `DefaultFeatureFlags`) is the one feature code actually depends on — this interface exists so
 * that facade never imports PostHog directly, the same reason `Trackable` exists for events.
 */
interface FeatureFlagSource {

    /** Fetches the latest flag values from the backend. A no-op source never has anything to fetch. */
    suspend fun refresh()

    /** [default] covers both "not fetched yet" and "unknown key" — never throws for a missing flag. */
    fun isEnabled(key: String, default: Boolean): Boolean

    /** Flips to `true` once a real [refresh] has completed, so observers know to re-check [isEnabled]. */
    fun flagsLoaded(): Flow<Boolean>
}

expect fun getFeatureFlagSource(apiKey: String?, contextProvider: AnalyticsContextProvider): FeatureFlagSource

/** Registered when the PostHog API key is blank — the same "off" state [com.germandebustamante.fuelio.core.analytics.impl.getPostHogTracker] falls back to. */
object NoOpFeatureFlagSource : FeatureFlagSource {

    private val loaded = MutableStateFlow(false)

    override suspend fun refresh() = Unit

    override fun isEnabled(key: String, default: Boolean): Boolean = default

    override fun flagsLoaded(): Flow<Boolean> = loaded.asStateFlow()
}
