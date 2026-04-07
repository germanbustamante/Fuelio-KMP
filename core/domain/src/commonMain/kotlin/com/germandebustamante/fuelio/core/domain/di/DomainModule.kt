package com.germandebustamante.fuelio.core.domain.di

import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetGasStationsByLocationUseCase(get()) }
}