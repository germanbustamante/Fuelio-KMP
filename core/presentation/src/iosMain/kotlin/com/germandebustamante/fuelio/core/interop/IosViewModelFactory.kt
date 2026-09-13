package com.germandebustamante.fuelio.core.interop

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.navigation.action.DefaultNavigator
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.app.state.AppViewModel
import com.germandebustamante.fuelio.feature.common.analytics.DeepLinkOpened
import com.germandebustamante.fuelio.feature.detail.state.GasStationDetailViewModel
import com.germandebustamante.fuelio.feature.favorites.state.FavoritesViewModel
import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import com.germandebustamante.fuelio.feature.map.state.MapViewModel
import com.germandebustamante.fuelio.feature.onboarding.state.OnboardingViewModel
import com.germandebustamante.fuelio.feature.settings.state.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    fun gasStations(): GasStationsViewModel = get()

    /**
     * Neither of these takes resolution parameters, so they would work through a plain Koin lookup
     * from Swift too — they live here so Swift has one place to obtain any ViewModel rather than two
     * different mechanisms depending on the screen.
     */
    fun settings(): SettingsViewModel = get()

    fun app(): AppViewModel = get()

    fun favorites(): FavoritesViewModel = get()

    fun onboarding(): OnboardingViewModel = get()

    fun map(): MapViewModel = get()

    fun gasStationDetail(gasStationId: String): GasStationDetailViewModel =
        get<GasStationDetailViewModel> { parametersOf(Destination.GasStationDetails(gasStationId)) }

    /** The shared navigator. One observer per app, owned by the root — see [Navigator]. */
    fun navigator(): Navigator = get<Navigator>()

    /**
     * `AnalyticsTracking.track` is `suspend`, and `AppRouter`'s deep-link handling has no ViewModel
     * (and so no `viewModelScope`) to launch it from — this fires it on its own scope instead of
     * exporting `track` itself to Swift for a single call site (rule 6 in CLAUDE.md).
     */
    fun trackDeepLinkOpened(uri: String, resolved: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            get<AnalyticsTracking>().track(DeepLinkOpened(uri, resolved))
        }
    }

    /**
     * A **private** navigator for tests.
     *
     * `Navigator.navigationActions` is `Channel`-backed and single-consumer, so a test subscribing to
     * the shared navigator would steal events from the running app's router.
     */
    fun isolatedNavigator(): Navigator = DefaultNavigator()
}
