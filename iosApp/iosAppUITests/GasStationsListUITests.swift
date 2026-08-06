import XCTest

/// Every element is located by accessibility identifier, never by localized text, so these keep
/// passing when the app runs in Spanish or a string changes.
final class GasStationsListUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testSearchFiltersTheList() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        let repsolRow = app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID))
        XCTAssertTrue(repsolRow.waitForExistence(timeout: 10))

        // `.searchable` builds its own field, which cannot carry an identifier, so it is located
        // structurally rather than by its (localized) placeholder.
        let searchField = app.searchFields.element(boundBy: 0)
        XCTAssertTrue(searchField.waitForExistence(timeout: 10))
        searchField.tap()
        searchField.typeText(UITestSupport.ballenoilStationName)

        // The 300 ms debounce lives in the ViewModel, so the filtering is not immediate.
        XCTAssertTrue(
            wait(repsolRow, satisfies: "exists == false", timeout: 10),
            "the searched-away row should disappear once the ViewModel's debounce elapses"
        )
    }

    func testChangingFuelChangesTheDisplayedPrice() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        let row = app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID))
        XCTAssertTrue(row.waitForExistence(timeout: 10))
        // The row merges its children for VoiceOver, so its label carries both fuel and price.
        let gasolineLabel = row.label

        let dieselOption = app.element(id: UITestSupport.fuelOption("diesel"))
        XCTAssertTrue(dieselOption.waitForExistence(timeout: 10))
        dieselOption.tap()

        XCTAssertTrue(
            wait(row, satisfies: "label != %@", gasolineLabel, timeout: 10),
            "switching fuel must change the price the ViewModel exposes"
        )
    }

    func testChangingProvinceUpdatesTheTitle() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        let provinceButton = app.element(id: UITestSupport.provinceButton)
        XCTAssertTrue(provinceButton.waitForExistence(timeout: 10))
        let originalLabel = provinceButton.label
        provinceButton.tap()

        let madrid = app.element(id: UITestSupport.provinceRow(UITestSupport.madridProvinceID))
        XCTAssertTrue(madrid.waitForExistence(timeout: 10))
        madrid.tap()

        XCTAssertTrue(
            wait(provinceButton, satisfies: "label != %@", originalLabel, timeout: 10),
            "selecting a province should update the navigation title"
        )
    }

    func testTogglingAFavoriteSticks() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        let row = app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID))
        XCTAssertTrue(row.waitForExistence(timeout: 10))
        let originalLabel = row.label

        app.element(id: UITestSupport.favoriteButton(UITestSupport.repsolStationID)).tap()

        XCTAssertTrue(
            wait(row, satisfies: "label != %@", originalLabel, timeout: 10),
            "the row's merged label should gain the Favorite marker"
        )
    }

    func testFailedLoadShowsTheErrorStateWithRetry() {
        let app = XCUIApplication().launchForUITests(simulateFailure: true)

        let errorState = app.element(id: UITestSupport.errorState)
        XCTAssertTrue(errorState.waitForExistence(timeout: 20), "a failing repository should block with the error state")
        XCTAssertTrue(app.element(id: UITestSupport.retryButton).waitForExistence(timeout: 5))
        XCTAssertFalse(app.element(id: UITestSupport.stationsList).exists)
    }
}
