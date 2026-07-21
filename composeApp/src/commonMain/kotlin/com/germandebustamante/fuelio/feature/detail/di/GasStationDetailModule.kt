package com.germandebustamante.fuelio.feature.detail.di

import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.detail.state.GasStationDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val gasStationDetailModule = module {
    viewModel { (route: Destination.GasStationDetails) ->
        GasStationDetailViewModel(route)
    }
}