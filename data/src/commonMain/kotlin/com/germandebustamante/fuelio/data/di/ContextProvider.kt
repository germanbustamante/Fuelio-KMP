package com.germandebustamante.fuelio.data.di

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ContextProvider(context: Any) {
    fun getContext(): Any
}
