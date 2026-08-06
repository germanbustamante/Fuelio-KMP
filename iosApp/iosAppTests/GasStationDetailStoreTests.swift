import Testing
@testable import Fuelio
import CorePresentation

@Suite("GasStationDetailStore", .serialized)
@MainActor
struct GasStationDetailStoreTests {

    @Test("Starts in the loading content state")
    func startsLoading() {
        let store = GasStationDetailStore(backend: SpyGasStationDetailBackend())

        #expect(store.content == .loading)
    }

    @Test("Publishes the loaded station and its schedule")
    func publishesLoadedStation() {
        let backend = SpyGasStationDetailBackend()
        let store = GasStationDetailStore(backend: backend)
        store.activate()
        let station = FakeGasStationsKt.fakeGasStations[0]

        backend.emit(.loadedFake(station: station))

        guard case .success(let loaded, let scheduleDays) = store.content else {
            Issue.record("expected a loaded detail state, got \(store.content)")
            return
        }
        #expect(loaded.id == station.id)
        #expect(scheduleDays.count == 7)
    }

    @Test("Maps a missing station to notFound rather than to an error")
    func mapsMissingStationToNotFound() {
        let backend = SpyGasStationDetailBackend()
        let store = GasStationDetailStore(backend: backend)
        store.activate()

        backend.emit(.notFoundFake)

        #expect(store.content == .notFound)
    }

    @Test("Routes back through the ViewModel instead of popping the stack")
    func routesBackThroughTheViewModel() {
        let backend = SpyGasStationDetailBackend()
        let store = GasStationDetailStore(backend: backend)

        store.goBack()

        #expect(backend.didGoBack)
    }

    @Test("Releases the Kotlin ViewModel when it is destroyed")
    func closesBackendOnDeinit() {
        let backend = SpyGasStationDetailBackend()
        do {
            let store = GasStationDetailStore(backend: backend)
            store.activate()
            #expect(!backend.didClose)
        }

        #expect(backend.didClose)
        #expect(backend.subscription.isCancelled)
    }
}
