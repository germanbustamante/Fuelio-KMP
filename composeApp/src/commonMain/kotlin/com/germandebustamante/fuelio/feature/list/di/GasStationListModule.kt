package com.germandebustamante.fuelio.feature.list.di

import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val gasStationListModule = module {
    viewModelOf(::GasStationsViewModel)
}
