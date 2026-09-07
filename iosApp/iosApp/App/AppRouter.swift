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
    private let handlesExternalUris: Bool
    private var observation: Task<Void, Never>?

    /// `handlesExternalUris` exists for the same reason as `IosViewModelFactory.isolatedNavigator()`:
    /// `ExternalUriHandler` is a Kotlin `object` with a single listener slot, and the unit-test bundle
    /// is hosted by this very app process — a router built inside a test would otherwise steal (and
    /// then, on deinit, clear) the running app's listener. Production never passes it.
    init(
        navigator: Navigator = IosViewModelFactory.shared.navigator(),
        handlesExternalUris: Bool = true
    ) {
        self.navigator = navigator
        self.handlesExternalUris = handlesExternalUris
    }

    /// `isolated deinit` (SE-0371) so teardown runs on the main actor: a plain `deinit` is
    /// nonisolated and could not touch `@MainActor` state. Cancelling the Swift `Task` now cancels
    /// the Kotlin collection too — that round trip is what `@NativeCoroutines` buys over the raw
    /// exported `Flow`, which could only be abandoned, never cancelled.
    isolated deinit {
        observation?.cancel()
        if handlesExternalUris {
            ExternalUriHandler.shared.listener = nil
        }
    }

    func start() {
        guard observation == nil else { return }
        registerExternalUriListener()
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

    /// External URIs bypass the `Navigator` on purpose: they are not a ViewModel action with
    /// analytics attached, they are the app being *entered* at a screen. Android does the same thing
    /// in `FuelioNavHost`'s `DisposableEffect`.
    ///
    /// Registering here rather than in a view is what gives it the app's lifetime: `start()` is
    /// already idempotent and `isolated deinit` is the only main-actor teardown point. Assigning the
    /// listener also flushes a URI that arrived earlier — on a cold launch `.onOpenURL` fires before
    /// `RootView.task { router.start() }` runs, which is the whole reason `ExternalUriHandler` buffers
    /// one pending URI.
    private func registerExternalUriListener() {
        guard handlesExternalUris else { return }
        ExternalUriHandler.shared.listener = { [weak self] uri in
            // Kotlin gives no thread guarantee for this callback (Android always delivers it on the
            // main thread from `onNewIntent`; here it is whatever thread delivered the URL), and
            // `path` is `@MainActor` state, so hop explicitly instead of assuming.
            Task { @MainActor in self?.openDeepLink(uri) }
        }
    }

    /// Replaces the **whole** path with the destination's synthetic back stack, mirroring
    /// `FuelioNavHost`'s `backStack.clear()` + `addAll(...)`. A deep link defines where the user *is*,
    /// so whatever was on screen before is not part of that story — and "back" must still walk up to
    /// the list instead of dropping the user out of the app.
    ///
    /// An unsupported or malformed URI is a no-op, never a crash: `parseDeepLink` returns nil and the
    /// user simply stays where they were.
    func openDeepLink(_ uri: String) {
        guard let destination = parseDeepLink(uri: uri) else { return }
        path = Route.syntheticStack(for: destination)
    }
}
