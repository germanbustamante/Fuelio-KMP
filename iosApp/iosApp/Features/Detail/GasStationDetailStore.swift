import Observation
import CorePresentation

/// Observable owner of the detail screen's Kotlin ViewModel — see `GasStationsStore` for the pattern.
@MainActor
@Observable
final class GasStationDetailStore {

    private(set) var state: GasStationDetailUIState

    private let backend: GasStationDetailBackend
    private var subscription: FlowSubscription?

    init(backend: GasStationDetailBackend) {
        self.backend = backend
        self.state = backend.currentState
    }

    convenience init(gasStationId: String) {
        self.init(backend: KotlinGasStationDetailBackend(gasStationId: gasStationId))
    }

    /// `isolated deinit` (SE-0371) — see `GasStationsStore` for why.
    isolated deinit {
        subscription?.cancel()
        backend.close()
    }

    func activate() {
        guard subscription == nil else { return }
        subscription = backend.observeState { [weak self] newState in
            MainActor.assumeIsolated { self?.state = newState }
        }
    }

    func deactivate() {
        subscription?.cancel()
        subscription = nil
    }

    var content: GasStationDetailContent { state.content }

    /// Goes through the ViewModel so the `Navigator` stays the single source of navigation truth,
    /// rather than popping the `NavigationStack` directly from the view.
    func goBack() { backend.goBack() }
}
