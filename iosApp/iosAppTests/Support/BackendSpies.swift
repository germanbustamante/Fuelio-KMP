import Foundation
@testable import Fuelio
import CorePresentation

/// Records the state subscription's lifetime.
final class SpySubscription: StateSubscription {

    private(set) var isCancelled = false

    func cancel() { isCancelled = true }
}

/// Test double for `GasStationsBackend`.
///
/// It records the calls the store makes and lets a test push arbitrary `GasStationsUIState`s in,
/// built from the fakes already exported by `:core:presentation` — Kotlin default arguments are not
/// exported, so hand-building a `GasStationsUIState` from Swift would mean passing all twelve
/// parameters.
@MainActor
final class SpyGasStationsBackend: GasStationsBackend {

    enum Call: Equatable {
        case detectLocation
        case setProvinceFilterVisible(Bool)
        case selectProvince(String)
        case selectFuelFilter(FuelKind)
        case search(String)
        case dismissError
        case dismissStaleDataError
        case dismissPermissionAlert
        case openAppSettings
        case refresh
        case retry
        case openStation(String)
        case toggleFavorite(String)
        case close
    }

    private(set) var calls: [Call] = []
    private(set) var subscription = SpySubscription()

    var currentState: GasStationsUIState
    private var onEach: ((GasStationsUIState) -> Void)?

    init(initialState: GasStationsUIState = GasStationsFakesKt.fakeGasStationsUIStateLoading) {
        self.currentState = initialState
    }

    /// Simulates an emission from the Kotlin `StateFlow`.
    func emit(_ state: GasStationsUIState) {
        currentState = state
        onEach?(state)
    }

    var isObserving: Bool { onEach != nil }

    func observeState(_ onEach: @escaping (GasStationsUIState) -> Void) -> any StateSubscription {
        self.onEach = onEach
        return subscription
    }

    func close() { calls.append(.close) }

    func detectLocation() { calls.append(.detectLocation) }
    func setProvinceFilterVisible(_ visible: Bool) { calls.append(.setProvinceFilterVisible(visible)) }
    func selectProvince(_ province: DomainProvinceBO) { calls.append(.selectProvince(province.id)) }
    func selectFuelFilter(_ filter: FuelFilter) { calls.append(.selectFuelFilter(FuelKind(filter))) }
    func search(_ query: String) { calls.append(.search(query)) }
    func dismissError() { calls.append(.dismissError) }
    func dismissStaleDataError() { calls.append(.dismissStaleDataError) }
    func dismissPermissionAlert() { calls.append(.dismissPermissionAlert) }
    func openAppSettings() { calls.append(.openAppSettings) }
    func refresh() { calls.append(.refresh) }
    func retry() { calls.append(.retry) }
    func openStation(id: String) { calls.append(.openStation(id)) }
    func toggleFavorite(id: String) { calls.append(.toggleFavorite(id)) }
}

@MainActor
final class SpyGasStationDetailBackend: GasStationDetailBackend {

    private(set) var didGoBack = false
    private(set) var didClose = false
    private(set) var subscription = SpySubscription()

    var currentState: GasStationDetailUIState
    private var onEach: ((GasStationDetailUIState) -> Void)?

    init(initialState: GasStationDetailUIState = .loadingFake) {
        self.currentState = initialState
    }

    func emit(_ state: GasStationDetailUIState) {
        currentState = state
        onEach?(state)
    }

    func observeState(_ onEach: @escaping (GasStationDetailUIState) -> Void) -> any StateSubscription {
        self.onEach = onEach
        return subscription
    }

    func close() { didClose = true }
    func goBack() { didGoBack = true }
}

extension GasStationDetailUIState {

    /// `GasStationDetailUIState` has no exported fake, and Kotlin default arguments do not cross the
    /// bridge, so every parameter is spelled out here.
    static var loadingFake: GasStationDetailUIState {
        GasStationDetailUIState(gasStation: nil, today: nil, isLoading: true)
    }

    static func loadedFake(
        station: DomainGasStationBO = FakeGasStationsKt.fakeGasStations[0],
        today: Kotlinx_datetimeDayOfWeek = Kotlinx_datetimeDayOfWeek.wednesday
    ) -> GasStationDetailUIState {
        GasStationDetailUIState(gasStation: station, today: today, isLoading: false)
    }

    static var notFoundFake: GasStationDetailUIState {
        GasStationDetailUIState(gasStation: nil, today: nil, isLoading: false)
    }
}
