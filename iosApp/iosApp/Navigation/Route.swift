import CorePresentation

/// SwiftUI-side counterpart of the shared `Destination`.
///
/// `NavigationStack` needs `Hashable` path elements, and the exported Kotlin `Destination` classes
/// are Objective-C objects whose `hash`/`isEqual:` come from the Kotlin `data class` — usable, but
/// they would leak framework types into every `navigationDestination(for:)`. A small Swift enum keeps
/// the navigation graph readable and exhaustively switchable.
///
/// `Destination.GasStations` has no `Route` case on purpose: it is the stack root, not a pushed
/// screen. A `Navigate(GasStations)` action therefore pops back to the root.
enum Route: Hashable {
    case gasStationDetail(id: String)
    case settings
    case favorites

    init?(_ destination: Destination) {
        switch onEnum(of: destination) {
        case .gasStationDetails(let details):
            self = .gasStationDetail(id: details.gasStationId)
        case .settings:
            self = .settings
        case .favorites:
            self = .favorites
        case .gasStations:
            return nil
        }
    }
}

extension Route {

    /// Routes to push for a deep link, in order.
    ///
    /// `buildSyntheticBackStack` returns the full stack *including* the root `GasStations`, which has
    /// no `Route` case because it is the `NavigationStack`'s root view rather than a pushed screen —
    /// `Route.init?` returns nil for it, so `compactMap` drops exactly that element and nothing else.
    static func syntheticStack(for destination: Destination) -> [Route] {
        buildSyntheticBackStack(target: destination).compactMap(Route.init)
    }
}

/// Swift-native view of `NavigationAction`, converted in one place like every other sealed type.
enum RouterAction: Equatable {
    case push(Route)
    case popToRoot
    case pop

    init?(_ action: NavigationAction) {
        switch onEnum(of: action) {
        case .back:
            self = .pop
        case .navigate(let navigate):
            if let route = Route(navigate.destination) {
                self = .push(route)
            } else {
                self = .popToRoot
            }
        }
    }
}
