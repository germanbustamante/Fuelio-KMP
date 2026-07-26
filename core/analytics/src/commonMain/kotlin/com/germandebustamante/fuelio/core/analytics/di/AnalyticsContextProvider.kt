package com.germandebustamante.fuelio.core.analytics.di

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AnalyticsContextProvider(context: Any) {
    fun getContext(): Any
}
