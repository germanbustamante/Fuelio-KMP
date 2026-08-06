import Testing
import Foundation
@testable import Fuelio
import CorePresentation

/// Integration coverage across the real bridge: Koin, the Kotlin ViewModels, `Navigator` and the
/// `Flow` subscription — no doubles.
///
/// These run inside the host app, which `LaunchArguments.usesDeterministicData` starts with the
/// in-memory Koin overrides (XCTest sets `XCTestConfigurationFilePath` in this process), so there is
/// no network, no Room and no permission prompt.
@Suite("Kotlin bridge integration", .serialized)
@MainActor
struct BridgeIntegrationTests {

    private static let firstFakeStationID = FakeGasStationsKt.fakeGasStations[0].id

    @Test("Runs against the deterministic Koin graph")
    func runsAgainstFakes() {
        #expect(LaunchArguments.usesDeterministicData, "unit tests must not hit the real repositories")
    }

    // MARK: - Flow bridge

    @Test("A real Kotlin StateFlow reaches Swift")
    func stateFlowReachesSwift() async throws {
        let binding = IosBindingFactory.shared.createGasStationsBinding()
        defer { binding.close() }

        var received: [GasStationsUIState] = []
        let subscription = binding.observeState { received.append($0) }
        defer { subscription.cancel() }

        try await waitUntil { !received.isEmpty }

        #expect(!received.isEmpty)
        #expect(received.last === binding.currentState)
    }

    @Test("Cancelling really stops the collection")
    func cancellingStopsCollection() async throws {
        let binding = IosBindingFactory.shared.createGasStationsBinding()
        defer { binding.close() }

        var count = 0
        let subscription = binding.observeState { _ in count += 1 }
        try await waitUntil { count > 0 }

        subscription.cancel()
        let countAtCancel = count
        binding.viewModel.onSearchQueryChanged(query: "repsol")
        binding.viewModel.onFuelFilterSelected(filter: FuelFilterDiesel.shared)
        try await Task.sleep(for: .milliseconds(700))

        #expect(subscription.isCancelled)
        #expect(count == countAtCancel)
    }

    // MARK: - Factory

    @Test("Builds the list ViewModel with every dependency resolved and loads the fake stations")
    func buildsListViewModel() async throws {
        let store = GasStationsStore()
        defer { store.deactivate() }
        store.activate()

        try await waitUntil {
            if case .success = store.content { return true }
            return false
        }

        guard case .success(let stations) = store.content else {
            Issue.record("expected the list to load, got \(store.content)")
            return
        }
        #expect(stations.count == FakeGasStationsKt.fakeGasStations.count)
        #expect(store.selectedProvince != nil)
    }

    @Test("Applies the fuel filter through the ViewModel, not in Swift")
    func appliesFuelFilterThroughTheViewModel() async throws {
        let store = GasStationsStore()
        defer { store.deactivate() }
        store.activate()
        try await waitUntil { if case .success = store.content { return true } else { return false } }

        store.selectFuel(.diesel)
        try await waitUntil { store.selectedFuel == .diesel }

        guard case .success(let stations) = store.content else {
            Issue.record("expected stations after switching fuel")
            return
        }
        let station = try #require(stations.first { $0.station.id == Self.firstFakeStationID })
        #expect(station.price == station.station.dieselPrice?.doubleValue)
    }

    @Test("Filters through the ViewModel's debounced search")
    func filtersThroughTheViewModel() async throws {
        let store = GasStationsStore()
        defer { store.deactivate() }
        store.activate()
        try await waitUntil { if case .success = store.content { return true } else { return false } }

        store.search("BALLENOIL")
        // The 300 ms debounce lives in the ViewModel, so the result is not immediate.
        try await waitUntil(timeout: .seconds(5)) {
            if case .success(let stations) = store.content { return stations.count == 1 }
            return false
        }

        guard case .success(let stations) = store.content else {
            Issue.record("expected a filtered list")
            return
        }
        #expect(stations.first?.station.name == "BALLENOIL")
    }

    @Test("Builds the detail ViewModel for the requested station")
    func buildsDetailViewModel() async throws {
        let store = GasStationDetailStore(gasStationId: Self.firstFakeStationID)
        defer { store.deactivate() }
        store.activate()

        try await waitUntil {
            if case .success = store.content { return true }
            return false
        }

        guard case .success(let station, let scheduleDays) = store.content else {
            Issue.record("expected the detail to load, got \(store.content)")
            return
        }
        #expect(station.id == Self.firstFakeStationID)
        #expect(scheduleDays.count == 7)
    }

    @Test("Reports notFound for a station that is not cached")
    func reportsNotFoundForUnknownStation() async throws {
        let store = GasStationDetailStore(gasStationId: "does-not-exist")
        defer { store.deactivate() }
        store.activate()

        try await waitUntil { store.content == .notFound }

        #expect(store.content == .notFound)
    }

    // MARK: - Navigation observer

    @Test("A Kotlin navigation action pushes the matching route into the router")
    func navigationActionReachesTheRouter() async throws {
        // An isolated navigator: the shared one is single-consumer and already has the running app's
        // router attached.
        let binding = IosBindingFactory.shared.createIsolatedNavigationBinding()
        let router = AppRouter(binding: binding)
        router.start()

        binding.requestNavigation(destination: DestinationGasStationDetails(gasStationId: "7153"))
        try await waitUntil { !router.path.isEmpty }

        #expect(router.path == [.gasStationDetail(id: "7153")])
    }

    @Test("Tapping a row asks the ViewModel to navigate, so analytics still fire")
    func rowTapGoesThroughTheViewModel() async throws {
        let binding = IosBindingFactory.shared.createIsolatedNavigationBinding()
        let router = AppRouter(binding: binding)
        router.start()

        // The list ViewModel navigates through the *shared* navigator, so this asserts the store
        // forwards the tap rather than pushing the stack itself; the full chain is covered by the
        // XCUITest.
        let backend = SpyGasStationsBackend()
        let store = GasStationsStore(backend: backend)
        store.openStation(id: "7153")

        #expect(backend.calls == [.openStation("7153")])
        #expect(router.path.isEmpty)
    }
}

/// Polls `condition` on the main actor until it holds or the timeout elapses.
///
/// Preferred over a fixed `Task.sleep`: the Kotlin side hops dispatchers (`Dispatchers.Default` for
/// the heavy work, `Dispatchers.Main` for delivery), so any fixed delay is either flaky or slow.
@MainActor
private func waitUntil(
    timeout: Duration = .seconds(10),
    _ condition: @MainActor () -> Bool
) async throws {
    let deadline = ContinuousClock.now.advanced(by: timeout)
    while ContinuousClock.now < deadline {
        if condition() { return }
        try await Task.sleep(for: .milliseconds(20))
    }
    Issue.record("timed out after \(timeout) waiting for the condition")
}
