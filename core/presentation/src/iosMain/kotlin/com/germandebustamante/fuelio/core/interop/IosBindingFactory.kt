package com.germandebustamante.fuelio.core.interop

import com.germandebustamante.fuelio.core.navigation.action.DefaultNavigator
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.detail.state.GasStationDetailViewModel
import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * The one entry point Swift uses to obtain screen bindings — `IosBindingFactory.shared.…`.
 *
 * Both ViewModels are registered in Koin with resolution parameters
 * (`viewModel { (controller: LocationPermissionController) -> … }` and
 * `viewModel { (route: Destination.GasStationDetails) -> … }`). Building a `ParametersHolder` from
 * Swift through the exported Koin API is possible but unreadable and untyped, so the parameter work
 * happens here and Swift only ever passes plain values.
 */
object IosBindingFactory : KoinComponent {

    fun createGasStationsBinding(): IosGasStationsBinding {
        val permissionController = get<LocationPermissionController>()
        val handle = createViewModelHandle(GasStationsViewModel::class) {
            get<GasStationsViewModel> { parametersOf(permissionController) }
        }
        return IosGasStationsBinding(handle)
    }

    fun createGasStationDetailBinding(gasStationId: String): IosGasStationDetailBinding {
        val route = Destination.GasStationDetails(gasStationId)
        val handle = createViewModelHandle(GasStationDetailViewModel::class) {
            get<GasStationDetailViewModel> { parametersOf(route) }
        }
        return IosGasStationDetailBinding(handle)
    }

    /** One per app, owned by the root — see [IosNavigationBinding]. */
    fun createNavigationBinding(): IosNavigationBinding = IosNavigationBinding(get<Navigator>())

    /**
     * A binding over a **private** [DefaultNavigator].
     *
     * `Navigator.navigationActions` is `Channel`-backed and single-consumer, so a Swift test that
     * subscribed to the shared navigator would steal events from the running app's router. Tests use
     * this instead, and drive it with [IosNavigationBinding.requestNavigation].
     */
    fun createIsolatedNavigationBinding(): IosNavigationBinding = IosNavigationBinding(DefaultNavigator())
}
