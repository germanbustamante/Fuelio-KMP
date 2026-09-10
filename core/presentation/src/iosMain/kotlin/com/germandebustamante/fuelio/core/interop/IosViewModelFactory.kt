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
 * The one entry point Swift uses to obtain a ViewModel — `IosViewModelFactory.shared.…`.
 *
 * This is all that remains of the iOS interop layer. KMP-ObservableViewModel owns the ViewModel's
 * lifetime (`@StateViewModel` clears it when the view goes away) and KMP-NativeCoroutines exposes
 * its state, but neither can resolve a Koin definition that takes **parameters**:
 *
 * ```
 * viewModel { (controller: LocationPermissionController) -> GasStationsViewModel(…) }
 * viewModel { (route: Destination.GasStationDetails) -> GasStationDetailViewModel(…) }
 * ```
 *
 * Building a `ParametersHolder` from Swift through the exported Koin API is possible but unreadable
 * and untyped, so the parameter work happens here and Swift only ever passes plain values.
 */
object IosViewModelFactory : KoinComponent {

    /**
     * `LocationPermissionController` is a Koin `single` on iOS only (see `presentationPlatformModule`);
     * Android injects its own Activity-bound implementation instead, which is why the definition takes
     * it as a parameter rather than resolving it internally.
     */
    fun gasStations(): GasStationsViewModel = get<GasStationsViewModel> { parametersOf(get<LocationPermissionController>()) }

    fun gasStationDetail(gasStationId: String): GasStationDetailViewModel =
        get<GasStationDetailViewModel> { parametersOf(Destination.GasStationDetails(gasStationId)) }

    /** The shared navigator. One observer per app, owned by the root — see [Navigator]. */
    fun navigator(): Navigator = get<Navigator>()

    /**
     * A **private** navigator for tests.
     *
     * `Navigator.navigationActions` is `Channel`-backed and single-consumer, so a test subscribing to
     * the shared navigator would steal events from the running app's router.
     */
    fun isolatedNavigator(): Navigator = DefaultNavigator()
}
