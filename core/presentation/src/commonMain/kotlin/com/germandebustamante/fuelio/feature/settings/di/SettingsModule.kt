package com.germandebustamante.fuelio.feature.settings.di

import com.germandebustamante.fuelio.feature.settings.state.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
}
