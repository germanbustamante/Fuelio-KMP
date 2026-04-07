package com.germandebustamante.fuelio.di

import com.germandebustamante.fuelio.core.domain.di.domainModule
import com.germandebustamante.fuelio.data.di.dataModule
import com.germandebustamante.fuelio.feature.list.di.gasStationListModule
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
    )
}
