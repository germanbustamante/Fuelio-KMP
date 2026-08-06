import Foundation

/// Accessibility identifiers and launch flags used by the UI tests.
///
/// The XCUITest target runs out of process and does not link the app module, so these values are
/// mirrored from `A11yID`/`LaunchArguments` rather than imported. `AccessibilityIdentifierParityTests`
/// in `iosAppTests` fails if the two ever diverge.
enum UITestSupport {

    static let uiTestMode = "-UITestMode"

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

    static let detailBackButton = "detail_back_button"
    static let detailStationName = "detail_station_name"
    static let detailNotFound = "detail_not_found"
    static let detailDirectionsButton = "detail_directions_button"
    static let detailScheduleSection = "detail_schedule_section"
    static let detailPricesSection = "detail_prices_section"

    static let permissionAlertSettings = "permission_alert_settings"
}
