package com.germandebustamante.fuelio.feature.app.di

import com.germandebustamante.fuelio.feature.app.state.AppViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AppViewModel(get()) }
}
