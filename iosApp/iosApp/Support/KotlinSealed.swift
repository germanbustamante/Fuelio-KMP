import CorePresentation

// The Objective-C exporter turns Kotlin `sealed interface`s into plain protocols, so Swift gets no
// exhaustiveness and every `switch` would need a `default` that silently swallows new variants.
//
// Every sealed type therefore gets converted to a real Swift enum **here and nowhere else**. Adding a
// variant on the Kotlin side then breaks exactly one file — this one — plus its tests, instead of
// disappearing into a `default` branch spread across the view layer.
//
// Note the exported names: `feature.list.state.ContentState` and `feature.detail.state.ContentState`
// collide once Objective-C flattens packages away, and the exporter disambiguates them as
// `ContentState_` (list) and `ContentState` (detail). Those names are an artifact of export order and
// must not appear anywhere outside this file.

// MARK: - Gas stations list content

enum GasStationsContent: Equatable {
    case initial
    case loading
    case success(stations: [GasStationItemVO])
    case empty
    case failure(message: String)

    init(_ kotlin: ContentState_) {
        switch kotlin {
        case is ContentState_Initial:
            self = .initial
        case is ContentState_Loading:
            self = .loading
        case let success as ContentState_Success:
            self = .success(stations: success.stations)
        case is ContentState_Empty:
            self = .empty
        case let failure as ContentState_Error:
            self = .failure(message: failure.message)
        default:
            assertionFailure("Unmapped list ContentState variant: \(type(of: kotlin))")
            self = .initial
        }
    }
}

extension GasStationsUIState {
    /// Swift-native view of `contentState`, safe to `switch` over exhaustively.
    var content: GasStationsContent { GasStationsContent(contentState) }
}

// MARK: - Gas station detail content

enum GasStationDetailContent: Equatable {
    case loading
    case success(station: DomainGasStationBO, scheduleDays: [ScheduleDayVO])
    /// `getGasStationById` is a local-only read, so "not found" means "not cached yet", not an error.
    case notFound

    init(_ kotlin: ContentState) {
        switch kotlin {
        case is ContentStateLoading:
            self = .loading
        case let success as ContentStateSuccess:
            self = .success(station: success.gasStation, scheduleDays: success.scheduleDays)
        case is ContentStateNotFound:
            self = .notFound
        default:
            assertionFailure("Unmapped detail ContentState variant: \(type(of: kotlin))")
            self = .notFound
        }
    }
}

extension GasStationDetailUIState {
    var content: GasStationDetailContent { GasStationDetailContent(contentState) }
}

// MARK: - Fuel filter

enum FuelKind: String, CaseIterable, Hashable, Identifiable {
    case gasoline95
    case gasoline98
    case diesel
    case dieselPremium

    var id: String { rawValue }

    var kotlin: FuelFilter {
        switch self {
        case .gasoline95: FuelFilterGasoline95.shared
        case .gasoline98: FuelFilterGasoline98.shared
        case .diesel: FuelFilterDiesel.shared
        case .dieselPremium: FuelFilterDieselPremium.shared
        }
    }

    init(_ kotlin: FuelFilter) {
        switch kotlin {
        case is FuelFilterGasoline95: self = .gasoline95
        case is FuelFilterGasoline98: self = .gasoline98
        case is FuelFilterDiesel: self = .diesel
        case is FuelFilterDieselPremium: self = .dieselPremium
        default:
            assertionFailure("Unmapped FuelFilter variant: \(type(of: kotlin))")
            self = .gasoline95
        }
    }
}

extension GasStationsUIState {
    var fuelKind: FuelKind { FuelKind(selectedFuelFilter) }
}

// MARK: - Weekly schedule

enum ScheduleStatus: Equatable {
    case closed
    case alwaysOpen
    case hours(start: String, end: String)

    init(_ kotlin: ScheduleDayStatus) {
        switch kotlin {
        case is ScheduleDayStatusClosed:
            self = .closed
        case is ScheduleDayStatusAlwaysOpen:
            self = .alwaysOpen
        case let hours as ScheduleDayStatusHours:
            self = .hours(start: hours.start, end: hours.end)
        default:
            assertionFailure("Unmapped ScheduleDayStatus variant: \(type(of: kotlin))")
            self = .closed
        }
    }
}

extension ScheduleDayVO {
    var scheduleStatus: ScheduleStatus { ScheduleStatus(status) }
}

// MARK: - Weekday

enum Weekday: Int, CaseIterable, Hashable {
    case monday, tuesday, wednesday, thursday, friday, saturday, sunday

    /// `DayOfWeek` exports as a `KotlinEnum` subclass, so identity is compared through `ordinal`
    /// rather than a Swift `case` pattern.
    init(_ kotlin: Kotlinx_datetimeDayOfWeek) {
        self = Weekday(rawValue: Int(kotlin.ordinal)) ?? .monday
    }
}

extension ScheduleDayVO {
    var weekday: Weekday { Weekday(dayOfWeek) }
}
