package com.germandebustamante.fuelio.core.di

import com.germandebustamante.fuelio.core.analytics.AnalyticsManager
import com.germandebustamante.fuelio.core.analytics.impl.getFirebaseTracker
import com.germandebustamante.fuelio.core.navigation.action.DefaultNavigator
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import org.koin.dsl.module

val coreModule = module {
    single<Navigator> { DefaultNavigator() }
    single { AnalyticsManager(listOf(getFirebaseTracker())) }
}
