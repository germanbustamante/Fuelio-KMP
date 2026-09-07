package com.germandebustamante.fuelio.core.testing

/**
 * Accessibility/test identifiers shared between both native UIs and their instrumentation suites.
 *
 * Android's `Modifier.testTag(...)` and iOS's `.accessibilityIdentifier(...)` are located by
 * identifier and never by localized text, so tests keep passing when a string changes or the app
 * runs in Spanish — but until now each platform kept its own hand-typed copy of these literals
 * (iOS's is `iosApp/iosApp/Support/AccessibilityIdentifiers.swift`). `const val` here is what makes
 * Kotlin/Native export these as plain static string constants Swift can read directly, the same
 * mechanism the design tokens use (ADR 0003) — one source of truth instead of two copies that can
 * silently drift apart.
 */
object A11yIdentifiers {

    // Gas stations list
    const val STATIONS_LIST = "gas_stations_list"
    const val SEARCH_FIELD = "station_search_field"
    const val FUEL_FILTER_PICKER = "fuel_filter_picker"
    const val PROVINCE_BUTTON = "province_button"
    const val PROVINCE_SHEET = "province_sheet"
    const val PROVINCE_SHEET_CLOSE = "province_sheet_close"
    const val DETECT_LOCATION_BUTTON = "detect_location_button"
    const val LOADING_SKELETON = "gas_stations_loading"
    const val EMPTY_STATE = "gas_stations_empty"
    const val ERROR_STATE = "gas_stations_error"
    const val RETRY_BUTTON = "gas_stations_retry"
    const val STALE_DATA_BANNER = "stale_data_banner"

    fun stationRow(stationId: String): String = "station_row_$stationId"
    fun stationPrice(stationId: String): String = "station_price_$stationId"
    fun favoriteButton(stationId: String): String = "favorite_button_$stationId"
    fun fuelOption(rawValue: String): String = "fuel_option_$rawValue"
    fun provinceRow(provinceId: String): String = "province_row_$provinceId"

    // Detail
    const val DETAIL_BACK_BUTTON = "detail_back_button"
    const val DETAIL_STATION_NAME = "detail_station_name"
    const val DETAIL_NOT_FOUND = "detail_not_found"
    const val DETAIL_DIRECTIONS_BUTTON = "detail_directions_button"
    const val DETAIL_SCHEDULE_SECTION = "detail_schedule_section"
    const val DETAIL_PRICES_SECTION = "detail_prices_section"

    // Permissions
    const val PERMISSION_ALERT_SETTINGS = "permission_alert_settings"
}
