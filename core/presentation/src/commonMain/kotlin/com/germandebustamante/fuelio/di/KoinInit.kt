package com.germandebustamante.fuelio.di

import com.germandebustamante.fuelio.core.analytics.di.analyticsModule
import com.germandebustamante.fuelio.core.di.coreModule
import com.germandebustamante.fuelio.core.domain.di.domainModule
import com.germandebustamante.fuelio.core.logger.CrashReporting
import com.germandebustamante.fuelio.core.startup.StartupTask
import com.germandebustamante.fuelio.core.startup.di.startupModule
import com.germandebustamante.fuelio.data.di.dataModule
import com.germandebustamante.fuelio.feature.app.di.appModule
import com.germandebustamante.fuelio.feature.detail.di.gasStationDetailModule
import com.germandebustamante.fuelio.feature.list.di.gasStationListModule
import com.germandebustamante.fuelio.feature.settings.di.settingsModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

/**
 * @param overrides modules applied last, so their definitions win over the production ones. Used by
 * the iOS UI-test harness to swap the repositories for in-memory fakes; production call sites pass
 * nothing. Declared **before** [config] so `initKoin { androidContext(this) }` keeps binding its
 * trailing lambda to [config].
 */
fun initKoin(overrides: List<Module> = emptyList(), config: KoinAppDeclaration? = null): KoinApplication = startKoin {
    includes(config)
    modules(
        domainModule,
        dataModule,
        gasStationListModule,
        gasStationDetailModule,
        settingsModule,
        appModule,
        analyticsModule,
        coreModule,
        startupModule,
        presentationPlatformModule,
    )
    modules(overrides)
}.also { app ->
    // Installed synchronously, before any task runs: CrashReporterStartupTask is itself one of the
    // tasks, and they all launch concurrently, so leaving the install to it would mean a task that
    // fails first has its failure reported to a no-op reporter.
    CrashReporting.install(app.koin.get())

    val startupTasks = app.koin.get<Set<StartupTask>>()
    MainScope().launch {
        startupTasks.forEach { task ->
            launch {
                runCatching { task() }.onFailure { CrashReporting.logError("StartupTask", "Startup task failed", it) }
            }
        }
    }
}
