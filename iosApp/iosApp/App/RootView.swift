import SwiftUI

/// App shell: one `NavigationStack` driven by the single `AppRouter`, which in turn is driven by the
/// shared Kotlin `Navigator`. Screens never push or pop directly — they call a ViewModel action, the
/// ViewModel emits a `NavigationAction`, and the router applies it. That keeps analytics and
/// navigation in one place across both platforms.
struct RootView: View {

    @LazyStore({ AppRouter() }) private var router

    var body: some View {
        @Bindable var router = router
        return NavigationStack(path: $router.path) {
            GasStationsScreen()
                .navigationDestination(for: Route.self) { route in
                    switch route {
                    case .gasStationDetail(let id):
                        GasStationDetailScreen(gasStationId: id)
                    }
                }
        }
        .fuelioTheme()
        .task { router.start() }
    }
}

#Preview {
    RootView()
}
