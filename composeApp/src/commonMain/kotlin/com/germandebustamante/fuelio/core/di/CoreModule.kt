package com.germandebustamante.fuelio.core.di

import com.germandebustamante.fuelio.core.navigation.action.DefaultNavigator
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import org.koin.dsl.module

val coreModule = module {
    single<Navigator> { DefaultNavigator() }
}
