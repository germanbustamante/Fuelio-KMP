import XCTest

/// Accessibility identifiers and launch flags used by the UI tests.
///
/// The XCUITest target runs out of process and does not link the app module, so these values are
/// mirrored from `A11yID`/`LaunchArguments` rather than imported.
/// `AccessibilityIdentifierContractTests` in `iosAppTests` pins the app side against this same table,
/// so a rename on either side fails clearly instead of as "element not found".
enum UITestSupport {

    static let uiTestMode = "-UITestMode"
    static let uiTestFailureMode = "-UITestFailure"
    static let uiTestDeepLink = "-UITestDeepLink"

    /// First entries of `core/fake/FakeGasStations.kt` and `feature/list/state/GasStationsFakes.kt`,
    /// which the UI-test Koin overrides serve.
    static let repsolStationID = "7153"
    static let ballenoilStationName = "Ballenoil"
    static let madridProvinceID = "2"
    static let stationCount = 16

    /// A station id absent from the fakes — `InMemoryGasStationRepository.getGasStationById` is a
    /// local-only lookup, so this deterministically reaches the detail screen's "not found" state.
    static let uncachedStationID = "000000"

    static func stationDetailDeepLink(_ stationID: String) -> String {
        "fuelio://station/\(stationID)/detail"
    }

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

    // Settings
    static let settingsButton = "settings_button"
    static let settingsScreen = "settings_screen"
    static let settingsBackButton = "settings_back_button"
    static let themePicker = "theme_picker"
    static let defaultFuelPicker = "default_fuel_picker"

    static func themeOption(_ rawValue: String) -> String { "theme_option_\(rawValue)" }
}

extension XCTestCase {

    /// Waits for `element` to satisfy `format`.
    ///
    /// Used instead of a fixed sleep because most of what these tests wait on is produced by Kotlin
    /// coroutines (the 300 ms search debounce, a dispatcher hop), so any fixed delay is either flaky
    /// or needlessly slow.
    func wait(
        _ element: XCUIElement,
        satisfies format: String,
        _ arguments: CVarArg...,
        timeout: TimeInterval
    ) -> Bool {
        let predicate = NSPredicate(format: format, argumentArray: arguments)
        let expectation = XCTNSPredicateExpectation(predicate: predicate, object: element)
        return XCTWaiter().wait(for: [expectation], timeout: timeout) == .completed
    }
}

extension XCUIApplication {

    /// Looks an element up by identifier regardless of the element *type* SwiftUI ends up exposing.
    /// A row that merges its children (`.accessibilityElement(children: .combine)`) surfaces as a
    /// button, a cell or an `other` depending on the traits applied, so pinning the query to one type
    /// makes tests brittle for no benefit.
    func element(id: String) -> XCUIElement {
        descendants(matching: .any).matching(identifier: id).firstMatch
    }

    /// Launches with the deterministic in-memory Koin graph.
    ///
    /// The unit-test bundle is hosted by this same app, so an instance may still be shutting down
    /// when the UI tests start; SpringBoard then rejects the launch with "Application failed
    /// preflight checks". Terminating first makes the launch deterministic.
    @discardableResult
    func launchForUITests(simulateFailure: Bool = false, deepLink: String? = nil) -> XCUIApplication {
        if state != .notRunning {
            terminate()
        }
        var arguments = [UITestSupport.uiTestMode]
        if simulateFailure { arguments.append(UITestSupport.uiTestFailureMode) }
        if let deepLink { arguments += [UITestSupport.uiTestDeepLink, deepLink] }
        launchArguments = arguments
        launch()
        return self
    }

    /// Waits for the station list to finish loading.
    @discardableResult
    func waitForStationList(file: StaticString = #filePath, line: UInt = #line) -> XCUIElement {
        let list = element(id: UITestSupport.stationsList)
        XCTAssertTrue(list.waitForExistence(timeout: 20), "the station list never appeared", file: file, line: line)
        return list
    }
}
