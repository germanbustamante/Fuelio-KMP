package com.germandebustamante.fuelio.feature.list.di

import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val gasStationListModule = module {
    viewModel { (permissionController: LocationPermissionController) ->
        GasStationsViewModel(
            getGasStationByLocationUseCase = get(),
            getProvincesUseCase = get(),
            locationPermissionController = permissionController,
            resolveProvinceByLocationUseCase = get(),
            navigator = get(),
            analyticsManager = get(),
        )
    }
}
