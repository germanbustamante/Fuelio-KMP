package com.germandebustamante.fuelio.core.startup.di

import com.germandebustamante.fuelio.core.startup.CrashReporterStartupTask
import com.germandebustamante.fuelio.core.startup.StartupTask
import org.koin.dsl.module

val startupModule = module {
    single { CrashReporterStartupTask(get(), get()) }

    // Koin has no Hilt-style @IntoSet multibinding, so this set is assembled by hand. Add new
    // StartupTask implementations here — but only for work with no natural screen owner; anything a
    // ViewModel already kicks off belongs in its launchStartupTasks(...) block instead.
    single<Set<StartupTask>> {
        setOf(
            get<CrashReporterStartupTask>(),
        )
    }
}
