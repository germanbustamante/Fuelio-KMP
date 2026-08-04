package com.germandebustamante.fuelio.di

import com.germandebustamante.fuelio.core.analytics.di.analyticsModule
import com.germandebustamante.fuelio.core.di.coreModule
import com.germandebustamante.fuelio.core.domain.di.domainModule
import com.germandebustamante.fuelio.core.logger.AppLogger
import com.germandebustamante.fuelio.core.startup.StartupTask
import com.germandebustamante.fuelio.core.startup.di.startupModule
import com.germandebustamante.fuelio.data.di.dataModule
import com.germandebustamante.fuelio.feature.detail.di.gasStationDetailModule
import com.germandebustamante.fuelio.feature.list.di.gasStationListModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication = startKoin {
    includes(config)
    modules(
        domainModule,
        dataModule,
        gasStationListModule,
        gasStationDetailModule,
        analyticsModule,
        coreModule,
        startupModule,
    )
}.also { app ->
    val startupTasks = app.koin.get<Set<StartupTask>>()
    MainScope().launch {
        startupTasks.forEach { task ->
            launch {
                runCatching { task() }.onFailure { AppLogger.e("StartupTask", "Startup task failed", it) }
            }
        }
    }
}
