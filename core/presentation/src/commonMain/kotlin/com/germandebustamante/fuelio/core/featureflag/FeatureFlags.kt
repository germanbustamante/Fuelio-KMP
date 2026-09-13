package com.germandebustamante.fuelio.core.featureflag

import kotlinx.coroutines.flow.Flow

/**
 * The facade feature code depends on — never `FeatureFlagSource`/PostHog directly, the same reason
 * ViewModels depend on `AnalyticsTracking` rather than the concrete `AnalyticsManager`.
 *
 * There is deliberately no `FeatureFlag` enum yet: the roadmap's first real flag
 * (`price_trend_chart`) gates a screen that doesn't exist until the product-screens block ships,
 * and an enum with one member nothing reads would be dead code before it's a real flag. Keyed by a
 * plain string + default for now — a gated feature can wrap its own key in a small constant/enum
 * later without this facade's shape needing to change.
 */
interface FeatureFlags {

    /** Emits [default] (or whatever the source already knows) immediately, then re-emits whenever
     * the source finishes a [FeatureFlagSource.refresh] — so no observer blocks on the network. */
    fun observe(key: String, default: Boolean): Flow<Boolean>

    fun isEnabled(key: String, default: Boolean): Boolean
}
