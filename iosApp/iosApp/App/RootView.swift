import SwiftUI
import CorePresentation
import KMPObservableViewModelSwiftUI

/// App shell: one `NavigationStack` driven by the single `AppRouter`, which in turn is driven by the
/// shared Kotlin `Navigator`. Screens never push or pop directly — they call a ViewModel action, the
/// ViewModel emits a `NavigationAction`, and the router applies it. That keeps analytics and
/// navigation in one place across both platforms.
struct RootView: View {

    @LazyStore({ AppRouter() }) private var router

    /// The theme has to wrap the whole `NavigationStack`, above any screen's ViewModel, so the shared
    /// `AppViewModel` holds it — Android resolves the same state around `FuelioNavHost`.
    @StateViewModel private var appViewModel = IosViewModelFactory.shared.app()

    var body: some View {
        Group {
            switch appViewModel.state.onboardingCompletionState {
            case .none:
                // First preferences read hasn't resolved yet — render nothing rather than flash the
                // station list before onboarding covers it. Same reasoning as Android's `App.kt`.
                Color.clear
            case .some(false):
                OnboardingScreen()
            case .some(true):
                navigationStack
            }
        }
        .fuelioTheme()
        .preferredColorScheme(appViewModel.state.themeMode.colorScheme)
    }

    private var navigationStack: some View {
        @Bindable var router = router
        return NavigationStack(path: $router.path) {
            GasStationsScreen()
                .navigationDestination(for: Route.self) { route in
                    switch route {
                    case .gasStationDetail(let id):
                        GasStationDetailScreen(gasStationId: id)
                    case .settings:
                        SettingsScreen()
                    case .favorites:
                        FavoritesScreen()
                    }
                }
        }
        .task { router.start() }
    }
}
