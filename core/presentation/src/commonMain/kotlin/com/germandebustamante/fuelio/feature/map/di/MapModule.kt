package com.germandebustamante.fuelio.feature.map.di

import com.germandebustamante.fuelio.feature.map.state.MapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {
    viewModel { MapViewModel(get(), get(), get(), get()) }
}
