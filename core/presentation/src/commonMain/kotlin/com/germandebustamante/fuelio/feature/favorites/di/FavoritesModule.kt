package com.germandebustamante.fuelio.feature.favorites.di

import com.germandebustamante.fuelio.feature.favorites.state.FavoritesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val favoritesModule = module {
    viewModel { FavoritesViewModel(get(), get(), get(), get(), get()) }
}
