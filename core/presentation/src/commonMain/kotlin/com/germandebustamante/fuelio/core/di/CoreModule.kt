package com.germandebustamante.fuelio.core.di

import com.germandebustamante.fuelio.core.featureflag.DefaultFeatureFlags
import com.germandebustamante.fuelio.core.featureflag.FeatureFlags
import com.germandebustamante.fuelio.core.navigation.action.DefaultNavigator
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import org.koin.dsl.module

val coreModule = module {
    single<Navigator> { DefaultNavigator() }
    single<FeatureFlags> { DefaultFeatureFlags(get()) }
}
