package com.germandebustamante.fuelio.core.featureflag

import com.germandebustamante.fuelio.core.analytics.featureflag.FeatureFlagSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultFeatureFlags(private val source: FeatureFlagSource) : FeatureFlags {

    override fun observe(key: String, default: Boolean): Flow<Boolean> = source.flagsLoaded().map { source.isEnabled(key, default) }

    override fun isEnabled(key: String, default: Boolean): Boolean = source.isEnabled(key, default)
}
