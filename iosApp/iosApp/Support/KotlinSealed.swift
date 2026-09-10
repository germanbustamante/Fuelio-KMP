import SwiftUI
import CorePresentation

// The Objective-C exporter turns Kotlin `sealed interface`s into plain protocols with no
// exhaustiveness. SKIE restores it: `onEnum(of:)` yields a real Swift enum with associated values, so
// **adding a variant on the Kotlin side is now a compile error here** rather than a `default` branch
// swallowing it at runtime.
//
// The conversion still happens **here and nowhere else**, so that compile error lands in one file
// instead of every view that switches on the state.
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
        switch onEnum(of: kotlin) {
        case .initial: self = .initial
        case .loading: self = .loading
        case .success(let success): self = .success(stations: success.stations)
        case .empty: self = .empty
        case .error(let failure): self = .failure(message: failure.message)
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
        switch onEnum(of: kotlin) {
        case .loading: self = .loading
        case .success(let success):
            self = .success(station: success.gasStation, scheduleDays: success.scheduleDays)
        case .notFound: self = .notFound
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
        switch onEnum(of: kotlin) {
        case .gasoline95: self = .gasoline95
        case .gasoline98: self = .gasoline98
        case .diesel: self = .diesel
        case .dieselPremium: self = .dieselPremium
        }
    }
}

extension GasStationsUIState {
    var fuelKind: FuelKind { FuelKind(selectedFuelFilter) }
}

/// `DomainFuelType` is the domain-level enum the *preferences* use, `FuelFilter` the presentation-level
/// sealed interface the *list* uses. SKIE exports the former as a plain Swift enum, so this is an
/// ordinary exhaustive `switch` rather than an `onEnum(of:)` — but it belongs here for the same
/// reason as everything else in this file: one place to change when Kotlin adds a fuel.
extension DomainThemeMode {
    /// `nil` is how SwiftUI says "follow the system", which is exactly what `.system` means — so the
    /// resolution that Android needs an `isSystemInDarkTheme()` call for is free here.
    var colorScheme: ColorScheme? {
        switch self {
        case .system: nil
        case .light: .light
        case .dark: .dark
        }
    }
}

extension DomainFuelType {
    var kind: FuelKind {
        switch self {
        case .gasoline95: .gasoline95
        case .gasoline98: .gasoline98
        case .diesel: .diesel
        case .dieselPremium: .dieselPremium
        }
    }
}

// MARK: - Weekly schedule

enum ScheduleStatus: Equatable {
    case closed
    case alwaysOpen
    case hours(start: String, end: String)

    init(_ kotlin: ScheduleDayStatus) {
        switch onEnum(of: kotlin) {
        case .closed: self = .closed
        case .alwaysOpen: self = .alwaysOpen
        case .hours(let hours): self = .hours(start: hours.start, end: hours.end)
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
