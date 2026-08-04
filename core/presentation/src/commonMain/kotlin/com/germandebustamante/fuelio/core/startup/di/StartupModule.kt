package com.germandebustamante.fuelio.core.startup.di

import com.germandebustamante.fuelio.core.startup.StartupTask
import org.koin.dsl.module

val startupModule = module {
    single<Set<StartupTask>> {
        setOf(
            // add new StartupTask implementations here as they're needed
        )
    }
}
