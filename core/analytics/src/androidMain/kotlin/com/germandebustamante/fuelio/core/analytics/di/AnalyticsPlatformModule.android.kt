package com.germandebustamante.fuelio.core.analytics.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val analyticsPlatformModule = module {
    single { AnalyticsContextProvider(androidContext()) }
}
