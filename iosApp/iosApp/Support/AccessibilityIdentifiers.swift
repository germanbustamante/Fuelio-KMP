import Foundation

/// Accessibility identifiers shared between the app and the XCUITest target.
///
/// UI tests locate elements by identifier and never by localized text — otherwise every test would
/// break the moment a string changes or the simulator runs in Spanish.
enum A11yID {

    // Gas stations list
    static let stationsList = "gas_stations_list"
    static let searchField = "station_search_field"
    static let fuelFilterPicker = "fuel_filter_picker"
    static let provinceButton = "province_button"
    static let provinceSheet = "province_sheet"
    static let provinceSheetClose = "province_sheet_close"
    static let detectLocationButton = "detect_location_button"
    static let loadingSkeleton = "gas_stations_loading"
    static let emptyState = "gas_stations_empty"
    static let errorState = "gas_stations_error"
    static let retryButton = "gas_stations_retry"
    static let staleDataBanner = "stale_data_banner"

    static func stationRow(_ stationID: String) -> String { "station_row_\(stationID)" }
    static func stationPrice(_ stationID: String) -> String { "station_price_\(stationID)" }
    static func favoriteButton(_ stationID: String) -> String { "favorite_button_\(stationID)" }
    static func fuelOption(_ rawValue: String) -> String { "fuel_option_\(rawValue)" }
    static func provinceRow(_ provinceID: String) -> String { "province_row_\(provinceID)" }

    // Detail
    static let detailBackButton = "detail_back_button"
    static let detailStationName = "detail_station_name"
    static let detailNotFound = "detail_not_found"
    static let detailDirectionsButton = "detail_directions_button"
    static let detailScheduleSection = "detail_schedule_section"
    static let detailPricesSection = "detail_prices_section"

    // Settings
    static let settingsButton = "settings_button"
    static let settingsScreen = "settings_screen"
    static let settingsBackButton = "settings_back_button"
    static let themePicker = "theme_picker"
    static let defaultFuelPicker = "default_fuel_picker"

    static func themeOption(_ rawValue: String) -> String { "theme_option_\(rawValue)" }

    // Permissions
    static let permissionAlertSettings = "permission_alert_settings"
}
