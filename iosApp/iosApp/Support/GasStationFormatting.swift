import SwiftUI
import CorePresentation

// Display helpers over the exported Kotlin models. Formatting — of numbers (`formatAsEuros`/
// `formatAsKilometers`) and of the station name (`GasStationBO.displayName`, exported directly as a
// native Swift property, no wrapper needed) — stays in Kotlin so Android and iOS cannot drift; only
// presentation choices that are genuinely platform-specific live here.

extension GasStationItemVO {

    var price: Double? { getCurrentFuelPrice()?.doubleValue }

    var distanceKilometers: Double? { distanceInKilometers?.doubleValue }

    var formattedPrice: String? { price.map { NumberFormatterKt.formatAsEuros($0) } }

    var formattedDistance: String? { distanceKilometers.map { NumberFormatterKt.formatAsKilometers($0) } }
}

extension FuelKind {

    var title: LocalizedStringKey {
        switch self {
        case .gasoline95: "Gasoline 95"
        case .gasoline98: "Gasoline 98"
        case .diesel: "Diesel"
        case .dieselPremium: "Diesel+"
        }
    }

    /// Plain `String` for building composed VoiceOver labels, where `LocalizedStringKey` cannot be
    /// interpolated.
    var localizedTitle: String {
        switch self {
        case .gasoline95: String(localized: "Gasoline 95")
        case .gasoline98: String(localized: "Gasoline 98")
        case .diesel: String(localized: "Diesel")
        case .dieselPremium: String(localized: "Diesel+")
        }
    }
}

extension Weekday {

    var localizedName: String {
        switch self {
        case .monday: String(localized: "Monday")
        case .tuesday: String(localized: "Tuesday")
        case .wednesday: String(localized: "Wednesday")
        case .thursday: String(localized: "Thursday")
        case .friday: String(localized: "Friday")
        case .saturday: String(localized: "Saturday")
        case .sunday: String(localized: "Sunday")
        }
    }
}
