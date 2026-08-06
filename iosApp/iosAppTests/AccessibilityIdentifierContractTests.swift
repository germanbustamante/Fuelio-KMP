import Testing
@testable import Fuelio

/// The XCUITest target runs out of process and cannot link the app module, so it mirrors these
/// identifiers in `iosAppUITests/UITestSupport.swift`. This suite pins the app side against the same
/// literal table: renaming an identifier in `A11yID` without updating the UI tests fails here, with a
/// clear message, instead of failing later as a mysterious "element not found".
@Suite("Accessibility identifier contract")
struct AccessibilityIdentifierContractTests {

    @Test("Static identifiers match the values the UI tests query")
    func staticIdentifiersMatch() {
        #expect(A11yID.stationsList == "gas_stations_list")
        #expect(A11yID.searchField == "station_search_field")
        #expect(A11yID.fuelFilterPicker == "fuel_filter_picker")
        #expect(A11yID.provinceButton == "province_button")
        #expect(A11yID.provinceSheet == "province_sheet")
        #expect(A11yID.provinceSheetClose == "province_sheet_close")
        #expect(A11yID.detectLocationButton == "detect_location_button")
        #expect(A11yID.loadingSkeleton == "gas_stations_loading")
        #expect(A11yID.emptyState == "gas_stations_empty")
        #expect(A11yID.errorState == "gas_stations_error")
        #expect(A11yID.retryButton == "gas_stations_retry")
        #expect(A11yID.staleDataBanner == "stale_data_banner")
        #expect(A11yID.detailBackButton == "detail_back_button")
        #expect(A11yID.detailStationName == "detail_station_name")
        #expect(A11yID.detailNotFound == "detail_not_found")
        #expect(A11yID.detailDirectionsButton == "detail_directions_button")
        #expect(A11yID.detailScheduleSection == "detail_schedule_section")
        #expect(A11yID.detailPricesSection == "detail_prices_section")
        #expect(A11yID.permissionAlertSettings == "permission_alert_settings")
    }

    @Test("Parameterised identifiers match the values the UI tests build")
    func parameterisedIdentifiersMatch() {
        #expect(A11yID.stationRow("7153") == "station_row_7153")
        #expect(A11yID.stationPrice("7153") == "station_price_7153")
        #expect(A11yID.favoriteButton("7153") == "favorite_button_7153")
        #expect(A11yID.fuelOption(FuelKind.diesel.rawValue) == "fuel_option_diesel")
        #expect(A11yID.provinceRow("1") == "province_row_1")
    }

    @Test("The UI-test launch flag matches the one the app reads")
    func launchFlagMatches() {
        #expect(LaunchArguments.uiTestMode == "-UITestMode")
    }
}
