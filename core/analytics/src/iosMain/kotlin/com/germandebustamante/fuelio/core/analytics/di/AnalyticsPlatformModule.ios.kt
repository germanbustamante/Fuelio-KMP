package com.germandebustamante.fuelio.core.analytics.di

import org.koin.dsl.module

actual val analyticsPlatformModule = module {
    single { AnalyticsContextProvider(Unit) }
}
