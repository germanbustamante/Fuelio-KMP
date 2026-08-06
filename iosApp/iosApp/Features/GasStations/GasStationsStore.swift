import Observation
import CorePresentation

/// Observable owner of the gas stations screen's Kotlin ViewModel.
///
/// The Kotlin `GasStationsUIState` is published as-is: re-mapping it into a parallel Swift struct
/// would duplicate the model and invite the two to drift. The store only owns the *subscription* and
/// the ViewModel's lifetime, and forwards actions.
@MainActor
@Observable
final class GasStationsStore {

    private(set) var state: GasStationsUIState

    private let backend: GasStationsBackend
    private var subscription: FlowSubscription?

    init(backend: GasStationsBackend) {
        self.backend = backend
        // Seeded synchronously so the first frame renders the real state instead of a blank screen.
        self.state = backend.currentState
    }

    /// Separate from `init(backend:)` because a default argument expression is evaluated in a
    /// nonisolated context and could not call the `@MainActor` backend initializer.
    convenience init() {
        self.init(backend: KotlinGasStationsBackend())
    }

    /// `isolated deinit` (SE-0371) so teardown runs on the main actor: a plain `deinit` is
    /// nonisolated and cannot touch `@MainActor` state. Without this, closing the binding would have
    /// to move to the view lifecycle and a dropped store would keep `viewModelScope` running.
    isolated deinit {
        subscription?.cancel()
        backend.close()
    }

    // MARK: - Lifecycle

    func activate() {
        guard subscription == nil else { return }
        subscription = backend.observeState { [weak self] newState in
            // Delivered on `Dispatchers.Main.immediate` by the bridge.
            MainActor.assumeIsolated { self?.state = newState }
        }
    }

    func deactivate() {
        subscription?.cancel()
        subscription = nil
    }

    // MARK: - Derived state

    var content: GasStationsContent { state.content }
    var selectedFuel: FuelKind { state.fuelKind }
    var isRefreshing: Bool { state.isRefreshing }
    var searchQuery: String { state.searchQuery }
    var provinces: [DomainProvinceBO] { state.provinces }
    var selectedProvince: DomainProvinceBO? { state.selectedProvince }
    var isProvincePickerPresented: Bool { state.showFilterProvince }
    var isPermissionAlertPresented: Bool { state.showPermissionDeniedPermanentlySnackbar }
    var hasStaleDataError: Bool { state.staleDataError != nil }

    /// `Set<String>` crosses the bridge as `NSSet`, so membership is checked through the Kotlin object
    /// rather than converted to a Swift `Set` on every row render.
    func isFavorite(_ stationID: String) -> Bool { state.favorites.contains(stationID) }

    // MARK: - Actions

    func detectLocation() { backend.detectLocation() }
    func presentProvincePicker() { backend.setProvinceFilterVisible(true) }
    func dismissProvincePicker() { backend.setProvinceFilterVisible(false) }
    func selectProvince(_ province: DomainProvinceBO) { backend.selectProvince(province) }
    func selectFuel(_ kind: FuelKind) { backend.selectFuelFilter(kind.kotlin) }
    /// The 300 ms debounce already lives in the ViewModel — do not add a second one here.
    func search(_ query: String) { backend.search(query) }
    func dismissError() { backend.dismissError() }
    func dismissStaleDataError() { backend.dismissStaleDataError() }
    func dismissPermissionAlert() { backend.dismissPermissionAlert() }
    func openAppSettings() { backend.openAppSettings() }
    func refresh() { backend.refresh() }
    func retry() { backend.retry() }
    /// Routed through the ViewModel, not the router, so the selection analytics event still fires.
    func openStation(id: String) { backend.openStation(id: id) }
    func toggleFavorite(id: String) { backend.toggleFavorite(id: id) }
}
