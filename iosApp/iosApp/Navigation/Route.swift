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

    init?(_ destination: Destination) {
        switch destination {
        case let details as DestinationGasStationDetails:
            self = .gasStationDetail(id: details.gasStationId)
        case is DestinationGasStations:
            return nil
        default:
            assertionFailure("Unmapped Destination variant: \(type(of: destination))")
            return nil
        }
    }
}

/// Swift-native view of `NavigationAction`, converted in one place like every other sealed type.
enum RouterAction: Equatable {
    case push(Route)
    case popToRoot
    case pop

    init?(_ action: NavigationAction) {
        switch action {
        case is NavigationActionBack:
            self = .pop
        case let navigate as NavigationActionNavigate:
            if let route = Route(navigate.destination) {
                self = .push(route)
            } else {
                self = .popToRoot
            }
        default:
            assertionFailure("Unmapped NavigationAction variant: \(type(of: action))")
            return nil
        }
    }
}
