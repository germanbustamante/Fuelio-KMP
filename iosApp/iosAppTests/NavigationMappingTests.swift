import Testing
@testable import Fuelio
import CorePresentation

@Suite("Navigation mapping")
struct NavigationMappingTests {

    @Test("Maps a detail destination to a pushable route")
    func mapsDetailDestination() {
        let route = Route(DestinationGasStationDetails(gasStationId: "7153"))

        #expect(route == .gasStationDetail(id: "7153"))
    }

    @Test("Has no route for the stack root")
    func hasNoRouteForRoot() {
        #expect(Route(DestinationGasStations.shared) == nil)
    }

    @Test("Maps Back to a pop")
    func mapsBack() {
        #expect(RouterAction(NavigationActionBack.shared) == .pop)
    }

    @Test("Maps a navigate action to a push")
    func mapsNavigate() {
        let action = NavigationActionNavigate(destination: DestinationGasStationDetails(gasStationId: "10608"))

        #expect(RouterAction(action) == .push(.gasStationDetail(id: "10608")))
    }

    @Test("Treats navigating to the root as popping to the root")
    func mapsNavigateToRoot() {
        let action = NavigationActionNavigate(destination: DestinationGasStations.shared)

        #expect(RouterAction(action) == .popToRoot)
    }

    @Test("Builds a synthetic stack that keeps the list underneath the detail")
    func buildsSyntheticStack() {
        let stack = Route.syntheticStack(for: DestinationGasStationDetails(gasStationId: "7153"))

        // The root `GasStations` is the NavigationStack's root view, not a pushed route, so it is
        // dropped: one pushed detail on top of the list is what "back returns to the list" means.
        #expect(stack == [.gasStationDetail(id: "7153")])
    }

    @Test("Builds an empty synthetic stack for the root destination")
    func buildsSyntheticStackForRoot() {
        #expect(Route.syntheticStack(for: DestinationGasStations.shared).isEmpty)
    }

    @Test("Parses a station detail deep link")
    func parsesStationDetailDeepLink() {
        let destination = parseDeepLink(uri: "fuelio://station/7153/detail")

        #expect(destination.flatMap(Route.init) == .gasStationDetail(id: "7153"))
    }

    @Test("Ignores an unsupported deep link")
    func ignoresUnsupportedDeepLink() {
        #expect(parseDeepLink(uri: "fuelio://station/7153/map") == nil)
    }
}

@Suite("AppRouter", .serialized)
@MainActor
struct AppRouterTests {

    private func makeRouter() -> AppRouter {
        // An isolated navigator: the shared one is `Channel`-backed and single-consumer, so
        // subscribing to it here would steal events from the app hosting these tests.
        // `handlesExternalUris: false` for the same reason: `ExternalUriHandler` has a single global
        // listener slot, and this test bundle is hosted by the very app whose listener is live.
        AppRouter(navigator: IosViewModelFactory.shared.isolatedNavigator(), handlesExternalUris: false)
    }

    @Test("Pushes and pops the navigation path")
    func pushesAndPops() {
        let router = makeRouter()

        router.apply(.push(.gasStationDetail(id: "7153")))
        #expect(router.path == [.gasStationDetail(id: "7153")])

        router.apply(.pop)
        #expect(router.path.isEmpty)
    }

    @Test("Popping an empty path is a no-op rather than a crash")
    func popOnEmptyPathIsSafe() {
        let router = makeRouter()

        router.apply(.pop)

        #expect(router.path.isEmpty)
    }

    @Test("Pops to the root from any depth")
    func popsToRoot() {
        let router = makeRouter()
        router.apply(.push(.gasStationDetail(id: "1")))
        router.apply(.push(.gasStationDetail(id: "2")))

        router.apply(.popToRoot)

        #expect(router.path.isEmpty)
    }

    @Test("A deep link replaces the whole path with its synthetic back stack")
    func deepLinkReplacesPath() {
        let router = makeRouter()
        router.apply(.push(.gasStationDetail(id: "1")))
        router.apply(.push(.gasStationDetail(id: "2")))

        router.openDeepLink("fuelio://station/7153/detail")

        #expect(router.path == [.gasStationDetail(id: "7153")])
    }

    @Test("An unsupported deep link leaves the path untouched")
    func unsupportedDeepLinkIsANoOp() {
        let router = makeRouter()
        router.apply(.push(.gasStationDetail(id: "1")))

        router.openDeepLink("https://example.com/whatever")

        #expect(router.path == [.gasStationDetail(id: "1")])
    }

    @Test("Registering the listener flushes a URI that arrived before the router started")
    func flushesPendingUri() async {
        // The one test that takes over the global `ExternalUriHandler` slot, so it restores it
        // afterwards. `.serialized` on the suite keeps it from racing the other cases here, but this
        // test bundle is hosted by the real app, whose own production `AppRouter` may already hold
        // the listener — clearing it immediately before `onNewUri` guarantees the URI is cached
        // rather than delivered to that other router, with nothing suspending in between the two
        // calls to let anything else claim the slot back.
        defer { ExternalUriHandler.shared.listener = nil }
        ExternalUriHandler.shared.listener = nil
        ExternalUriHandler.shared.onNewUri(uri: "fuelio://station/7153/detail")

        let router = AppRouter(
            navigator: IosViewModelFactory.shared.isolatedNavigator(),
            handlesExternalUris: true
        )
        router.start()

        // The listener hops to the main actor via `Task { @MainActor in ... }`, so poll instead of a
        // fixed sleep or a single `Task.yield()` (which is not guaranteed to run it in one hop).
        let deadline = ContinuousClock.now.advanced(by: .seconds(2))
        while router.path.isEmpty, ContinuousClock.now < deadline {
            await Task.yield()
        }

        #expect(router.path == [.gasStationDetail(id: "7153")])
    }
}
