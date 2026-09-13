package com.germandebustamante.fuelio.feature.list.di

import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * `LocationPermissionController` resolves as a plain `get()` on both platforms: iOS registers it as
 * a `single` directly, Android through `LocationPermissionControllerHolder` (see
 * `PresentationPlatformModule.android.kt`) — neither needs it passed in as a Koin resolution
 * parameter anymore.
 */
val gasStationListModule = module {
    viewModel {
        GasStationsViewModel(
            getGasStationByLocationUseCase = get(),
            getProvincesUseCase = get(),
            locationPermissionController = get(),
            resolveProvinceByLocationUseCase = get(),
            observeFavoriteStationIdsUseCase = get(),
            toggleFavoriteStationUseCase = get(),
            observeUserPreferencesUseCase = get(),
            setDefaultFuelTypeUseCase = get(),
            setSavedProvinceUseCase = get(),
            navigator = get(),
            analyticsManager = get(),
        )
    }
}
