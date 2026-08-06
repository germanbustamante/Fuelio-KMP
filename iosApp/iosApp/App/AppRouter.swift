import Observation
import CorePresentation
import KMPNativeCoroutinesAsync

/// Owns the `NavigationStack` path and is the **only** consumer of `Navigator.navigationActions`.
///
/// That flow is `Channel`-backed, so a second subscriber would steal events and navigation would drop
/// silently. Exactly one `AppRouter` exists, created at the root and alive for the whole app session.
@MainActor
@Observable
final class AppRouter {

    var path: [Route] = []

    private let navigator: Navigator
    private var observation: Task<Void, Never>?

    init(navigator: Navigator = IosViewModelFactory.shared.navigator()) {
        self.navigator = navigator
    }

    /// `isolated deinit` (SE-0371) so teardown runs on the main actor: a plain `deinit` is
    /// nonisolated and could not touch `@MainActor` state. Cancelling the Swift `Task` now cancels
    /// the Kotlin collection too — that round trip is what `@NativeCoroutines` buys over the raw
    /// exported `Flow`, which could only be abandoned, never cancelled.
    isolated deinit {
        observation?.cancel()
    }

    func start() {
        guard observation == nil else { return }
        observation = Task { [weak self] in
            guard let sequence = self?.navigator.navigationActions else { return }
            do {
                for try await action in asyncSequence(for: sequence) {
                    guard let self, let routerAction = RouterAction(action) else { continue }
                    self.apply(routerAction)
                }
            } catch is CancellationError {
                // Expected on teardown.
            } catch {
                assertionFailure("Navigation flow failed: \(error)")
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
