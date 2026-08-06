import Testing
@testable import Fuelio
import CorePresentation

@Suite("GasStationsStore", .serialized)
@MainActor
struct GasStationsStoreTests {

    @Test("Seeds its state synchronously so the first frame is not blank")
    func seedsInitialState() {
        let backend = SpyGasStationsBackend(initialState: GasStationsFakesKt.fakeGasStationsUIState)

        let store = GasStationsStore(backend: backend)

        #expect(store.state === GasStationsFakesKt.fakeGasStationsUIState)
        #expect(store.content == .success(stations: GasStationsFakesKt.fakeGasStationsUIState.gasStations))
    }

    @Test("Publishes every emission from the backend")
    func publishesEmissions() {
        let backend = SpyGasStationsBackend()
        let store = GasStationsStore(backend: backend)
        store.activate()

        backend.emit(GasStationsFakesKt.fakeGasStationsUIStateError)

        #expect(store.content == .failure(message: "Server error: 403"))
    }

    @Test("Subscribes only once even if activated repeatedly")
    func activatesOnce() {
        let backend = SpyGasStationsBackend()
        let store = GasStationsStore(backend: backend)

        store.activate()
        let first = backend.subscription
        store.activate()

        #expect(backend.subscription === first)
    }

    @Test("Cancels its subscription on deactivate")
    func deactivateCancels() {
        let backend = SpyGasStationsBackend()
        let store = GasStationsStore(backend: backend)
        store.activate()

        store.deactivate()

        #expect(backend.subscription.isCancelled)
    }

    @Test("Releases the Kotlin ViewModel when it is destroyed")
    func closesBackendOnDeinit() {
        let backend = SpyGasStationsBackend()
        do {
            let store = GasStationsStore(backend: backend)
            store.activate()
            #expect(!backend.calls.contains(.close))
        }

        // `isolated deinit` hops to the main actor; this test body already runs there, so the hop is
        // synchronous and the effect is observable immediately after the scope ends.
        #expect(backend.calls.contains(.close))
        #expect(backend.subscription.isCancelled)
    }

    @Test("Delegates every action to the ViewModel unchanged")
    func delegatesActions() {
        let backend = SpyGasStationsBackend(initialState: GasStationsFakesKt.fakeGasStationsUIState)
        let store = GasStationsStore(backend: backend)
        let province = GasStationsFakesKt.fakeProvinces[1]

        store.detectLocation()
        store.presentProvincePicker()
        store.dismissProvincePicker()
        store.selectProvince(province)
        store.selectFuel(.diesel)
        store.search("repsol")
        store.dismissError()
        store.dismissStaleDataError()
        store.dismissPermissionAlert()
        store.openAppSettings()
        store.refresh()
        store.retry()
        store.openStation(id: "7153")
        store.toggleFavorite(id: "7153")

        #expect(backend.calls == [
            .detectLocation,
            .setProvinceFilterVisible(true),
            .setProvinceFilterVisible(false),
            .selectProvince(province.id),
            .selectFuelFilter(.diesel),
            .search("repsol"),
            .dismissError,
            .dismissStaleDataError,
            .dismissPermissionAlert,
            .openAppSettings,
            .refresh,
            .retry,
            .openStation("7153"),
            .toggleFavorite("7153"),
        ])
    }

    @Test("Derives its view-facing properties straight from the Kotlin state")
    func derivesState() {
        let state = GasStationsFakesKt.fakeGasStationsUIStateShowModalSheet
        let store = GasStationsStore(backend: SpyGasStationsBackend(initialState: state))

        #expect(store.isProvincePickerPresented)
        #expect(store.selectedProvince?.id == GasStationsFakesKt.fakeProvince.id)
        #expect(store.provinces.count == GasStationsFakesKt.fakeProvinces.count)
        #expect(store.selectedFuel == .gasoline95)
        #expect(store.searchQuery.isEmpty)
        #expect(!store.hasStaleDataError)
        #expect(!store.isPermissionAlertPresented)
    }

    @Test("Reads favorites through the Kotlin Set, which crosses as an NSSet")
    func readsFavorites() {
        let base = GasStationsFakesKt.fakeGasStationsUIState
        let withFavorite = base.withFavoriteToggled(stationId: "7153")
        let store = GasStationsStore(backend: SpyGasStationsBackend(initialState: withFavorite))

        #expect(store.isFavorite("7153"))
        #expect(!store.isFavorite("10608"))
    }

    @Test("Surfaces the permission alert flag")
    func surfacesPermissionAlert() {
        let state = GasStationsFakesKt.fakeGasStationsUIStatePermissionSnackbar
        let store = GasStationsStore(backend: SpyGasStationsBackend(initialState: state))

        #expect(store.isPermissionAlertPresented)
    }
}
