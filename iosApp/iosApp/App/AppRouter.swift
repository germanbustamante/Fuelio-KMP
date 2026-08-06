import Observation
import CorePresentation

/// Owns the `NavigationStack` path and is the **only** consumer of `Navigator.navigationActions`.
///
/// That flow is `Channel`-backed, so a second subscriber would steal events and navigation would drop
/// silently. Exactly one `AppRouter` exists, created at the root and alive for the whole app session.
@MainActor
@Observable
final class AppRouter {

    var path: [Route] = []

    private let binding: IosNavigationBinding
    private var subscription: FlowSubscription?

    init(binding: IosNavigationBinding = IosBindingFactory.shared.createNavigationBinding()) {
        self.binding = binding
    }

    /// `isolated deinit` (SE-0371) so teardown runs on the main actor — see `GasStationsStore`.
    /// `binding.close()` cancels the Kotlin collection; without it the coroutine keeps the Swift
    /// callback (and anything it captures) alive past this object's lifetime.
    isolated deinit {
        subscription?.cancel()
        binding.close()
    }

    func start() {
        guard subscription == nil else { return }
        subscription = binding.observeNavigation { [weak self] action in
            // The bridge collects on `Dispatchers.Main.immediate`, so this is already the main thread;
            // `assumeIsolated` documents that contract and traps loudly if it is ever broken.
            MainActor.assumeIsolated {
                guard let self, let routerAction = RouterAction(action) else { return }
                self.apply(routerAction)
            }
        }
    }

    func apply(_ action: RouterAction) {
        switch action {
        case .push(let route):
            path.append(route)
        case .pop:
            if !path.isEmpty { path.removeLast() }
        case .popToRoot:
            path.removeAll()
        }
    }
}
