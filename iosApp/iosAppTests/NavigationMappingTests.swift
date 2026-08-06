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
}

@Suite("AppRouter", .serialized)
@MainActor
struct AppRouterTests {

    private func makeRouter() -> AppRouter {
        // An isolated navigator: the shared one is `Channel`-backed and single-consumer, so
        // subscribing to it here would steal events from the app hosting these tests.
        AppRouter(navigator: IosViewModelFactory.shared.isolatedNavigator())
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
}
