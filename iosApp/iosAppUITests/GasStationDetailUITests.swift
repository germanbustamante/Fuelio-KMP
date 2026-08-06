import XCTest

final class GasStationDetailUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testDetailShowsPricesScheduleAndDirections() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID)).tap()

        XCTAssertTrue(app.element(id: UITestSupport.detailStationName).waitForExistence(timeout: 10))
        XCTAssertTrue(app.element(id: UITestSupport.detailPricesSection).waitForExistence(timeout: 5))
        XCTAssertTrue(app.element(id: UITestSupport.detailScheduleSection).waitForExistence(timeout: 5))
        XCTAssertTrue(app.element(id: UITestSupport.detailDirectionsButton).waitForExistence(timeout: 5))
    }

    func testBackReturnsToTheList() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID)).tap()
        let backButton = app.element(id: UITestSupport.detailBackButton)
        XCTAssertTrue(backButton.waitForExistence(timeout: 10))

        backButton.tap()

        // Back goes through the ViewModel and the shared `Navigator`, not by popping the stack
        // directly, so this also proves that round trip works.
        XCTAssertTrue(
            wait(app.element(id: UITestSupport.stationsList), satisfies: "exists == true", timeout: 10),
            "onBackClick() should navigate back through the shared Navigator"
        )
    }

    // The detail "not found" state is unreachable through the UI on purpose — the list only offers
    // stations that are already cached — so it is covered by `BridgeIntegrationTests` instead.
}
