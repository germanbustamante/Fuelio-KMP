package com.germandebustamante.fuelio.core.analytics.di

import com.germandebustamante.fuelio.core.analytics.AnalyticsManager
import com.germandebustamante.fuelio.core.analytics.AnalyticsSecrets
import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.analytics.impl.getFirebaseTracker
import com.germandebustamante.fuelio.core.analytics.impl.getPostHogTracker
import org.koin.dsl.module

val analyticsModule = module {
    includes(analyticsPlatformModule)
    single<AnalyticsTracking> {
        AnalyticsManager(
            listOfNotNull(
                getFirebaseTracker(),
                getPostHogTracker(apiKey = AnalyticsSecrets.POSTHOG_API_KEY.ifBlank { null }, contextProvider = get()),
            ),
        )
    }
}
