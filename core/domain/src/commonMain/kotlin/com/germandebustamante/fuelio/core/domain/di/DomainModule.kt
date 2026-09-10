package com.germandebustamante.fuelio.core.domain.di

import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetDefaultFuelTypeUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetSavedProvinceUseCase
import com.germandebustamante.fuelio.core.domain.preferences.usecase.SetThemeModeUseCase
import com.germandebustamante.fuelio.core.domain.province.usecase.GetProvincesUseCase
import com.germandebustamante.fuelio.core.domain.province.usecase.ResolveProvinceByLocationUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetGasStationsByLocationUseCase(get()) }
    factory { GetGasStationUseCase(get()) }
    factory { GetProvincesUseCase(get()) }
    factory { ResolveProvinceByLocationUseCase() }
    factory { ObserveUserPreferencesUseCase(get()) }
    factory { SetDefaultFuelTypeUseCase(get()) }
    factory { SetSavedProvinceUseCase(get()) }
    factory { SetThemeModeUseCase(get()) }
}
