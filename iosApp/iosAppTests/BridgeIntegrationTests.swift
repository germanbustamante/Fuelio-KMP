import Testing
import Foundation
@testable import Fuelio
import CorePresentation
import KMPNativeCoroutinesAsync

/// Integration coverage across the real interop stack: Koin, the Kotlin ViewModels, `Navigator` and
/// the generated native flows — no doubles.
///
/// These run inside the host app, which `LaunchArguments.usesDeterministicData` starts with the
/// in-memory Koin overrides (XCTest sets `XCTestConfigurationFilePath` in this process), so there is
/// no network, no Room and no permission prompt.
@Suite("Kotlin interop integration", .serialized)
@MainActor
struct BridgeIntegrationTests {

    private static let firstFakeStationID = FakeGasStationsKt.fakeGasStations[0].id

    @Test("Runs against the deterministic Koin graph")
    func runsAgainstFakes() {
        #expect(LaunchArguments.usesDeterministicData, "unit tests must not hit the real repositories")
    }

    // MARK: - Generated native flow

    @Test("A real Kotlin StateFlow reaches Swift as an AsyncSequence")
    func stateFlowReachesSwift() async throws {
        let viewModel = IosViewModelFactory.shared.gasStations()

        var received: [GasStationsUIState] = []
        let task = Task {
            for try await state in asyncSequence(for: viewModel.stateFlow) {
                received.append(state)
            }
        }
        defer { task.cancel() }

        try await waitUntil { !received.isEmpty }

        #expect(!received.isEmpty)
        // `@NativeCoroutinesState` exposes the same state twice: as a stream and as a plain
        // property. They must agree, otherwise views reading `state` would lag the stream.
        #expect(received.last === viewModel.state)
    }

    @Test("Cancelling the Swift task really stops the Kotlin collection")
    func cancellingStopsCollection() async throws {
        let viewModel = IosViewModelFactory.shared.gasStations()

        var count = 0
        let task = Task {
            for try await _ in asyncSequence(for: viewModel.stateFlow) {
                count += 1
            }
        }
        try await waitUntil { count > 0 }

        task.cancel()
        // Let the cancellation propagate across the bridge before provoking more emissions.
        try await Task.sleep(for: .milliseconds(100))
        let countAtCancel = count

        viewModel.onSearchQueryChanged(query: "repsol")
        viewModel.onFuelFilterSelected(filter: FuelFilterDiesel.shared)
        try await Task.sleep(for: .milliseconds(700))

        #expect(count == countAtCancel)
    }

    // MARK: - Factory

    @Test("Builds the list ViewModel with every dependency resolved and loads the fake stations")
    func buildsListViewModel() async throws {
        let viewModel = IosViewModelFactory.shared.gasStations()

        try await waitUntil {
            if case .success = viewModel.state.content { return true }
            return false
        }

        guard case .success(let stations) = viewModel.state.content else {
            Issue.record("expected the list to load, got \(viewModel.state.content)")
            return
        }
        #expect(stations.count == FakeGasStationsKt.fakeGasStations.count)
        #expect(viewModel.state.selectedProvince != nil)
    }

    @Test("Applies the fuel filter through the ViewModel, not in Swift")
    func appliesFuelFilterThroughTheViewModel() async throws {
        let viewModel = IosViewModelFactory.shared.gasStations()
        try await waitUntil {
            if case .success = viewModel.state.content { return true } else { return false }
        }

        viewModel.onFuelFilterSelected(filter: FuelKind.diesel.kotlin)
        try await waitUntil { viewModel.state.fuelKind == .diesel }

        guard case .success(let stations) = viewModel.state.content else {
            Issue.record("expected stations after switching fuel")
            return
        }
        let station = try #require(stations.first { $0.station.id == Self.firstFakeStationID })
        #expect(station.price == station.station.dieselPrice?.doubleValue)
    }

    @Test("Filters through the ViewModel's debounced search")
    func filtersThroughTheViewModel() async throws {
        let viewModel = IosViewModelFactory.shared.gasStations()
        try await waitUntil {
            if case .success = viewModel.state.content { return true } else { return false }
        }

        viewModel.onSearchQueryChanged(query: "BALLENOIL")
        // The 300 ms debounce lives in the ViewModel, so the result is not immediate.
        try await waitUntil(timeout: .seconds(5)) {
            if case .success(let stations) = viewModel.state.content { return stations.count == 1 }
            return false
        }

        guard case .success(let stations) = viewModel.state.content else {
            Issue.record("expected a filtered list")
            return
        }
        #expect(stations.first?.station.name == "BALLENOIL")
    }

    @Test("Builds the detail ViewModel for the requested station")
    func buildsDetailViewModel() async throws {
        let viewModel = IosViewModelFactory.shared.gasStationDetail(gasStationId: Self.firstFakeStationID)

        try await waitUntil {
            if case .success = viewModel.state.content { return true }
            return false
        }

        guard case .success(let station, let scheduleDays) = viewModel.state.content else {
            Issue.record("expected the detail to load, got \(viewModel.state.content)")
            return
        }
        #expect(station.id == Self.firstFakeStationID)
        #expect(scheduleDays.count == 7)
    }

    @Test("Reports notFound for a station that is not cached")
    func reportsNotFoundForUnknownStation() async throws {
        let viewModel = IosViewModelFactory.shared.gasStationDetail(gasStationId: "does-not-exist")

        try await waitUntil { viewModel.state.content == .notFound }

        #expect(viewModel.state.content == .notFound)
    }

    // MARK: - Navigation observer

    @Test("A Kotlin navigation action pushes the matching route into the router")
    func navigationActionReachesTheRouter() async throws {
        // An isolated navigator: the shared one is single-consumer and already has the running app's
        // router attached.
        let navigator = IosViewModelFactory.shared.isolatedNavigator()
        let router = AppRouter(navigator: navigator)
        router.start()
        // `navigationActions` is rendezvous-backed: give the router's task a turn to actually attach
        // before emitting, exactly as the app does (the router starts at launch, navigation happens
        // later).
        try await Task.sleep(for: .milliseconds(200))

        try await navigator.navigate(destination: DestinationGasStationDetails(gasStationId: "7153"))
        try await waitUntil { !router.path.isEmpty }

        #expect(router.path == [.gasStationDetail(id: "7153")])
    }

    @Test("Tapping a row asks the ViewModel to navigate, so analytics still fire")
    func rowTapGoesThroughTheViewModel() async throws {
        let viewModel = IosViewModelFactory.shared.gasStations()
        try await waitUntil {
            if case .success = viewModel.state.content { return true } else { return false }
        }

        // A router over an isolated navigator must stay empty: the tap goes through the ViewModel and
        // the *shared* navigator, never by pushing the stack from the view. The full chain is covered
        // by the XCUITest.
        let router = AppRouter(navigator: IosViewModelFactory.shared.isolatedNavigator())
        router.start()

        viewModel.onItemClick(stationId: Self.firstFakeStationID)
        try await Task.sleep(for: .milliseconds(300))

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
