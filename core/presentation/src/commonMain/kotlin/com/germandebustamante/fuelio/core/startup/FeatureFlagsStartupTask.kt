package com.germandebustamante.fuelio.core.startup

import com.germandebustamante.fuelio.core.analytics.featureflag.FeatureFlagSource

/** Kicks off the one network fetch `FeatureFlags.observe`/`isEnabled` rely on to see real values. */
class FeatureFlagsStartupTask(private val featureFlagSource: FeatureFlagSource) : StartupTask {

    override suspend fun invoke() {
        featureFlagSource.refresh()
    }
}
