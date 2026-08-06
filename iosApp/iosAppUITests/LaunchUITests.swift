import XCTest

/// XCUITest stays on XCTest — Swift Testing does not host UI automation.
final class LaunchUITests: XCTestCase {

    /// First entry of `core/fake/FakeGasStations.kt`, which the UI-test Koin overrides serve.
    private static let firstFakeStationID = "7153"

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testAppLaunchesAndShowsTheStationList() {
        let app = XCUIApplication().launchForUITests()

        XCTAssertEqual(app.state, .runningForeground)
        XCTAssertTrue(
            app.element(id: UITestSupport.stationsList).waitForExistence(timeout: 15),
            "the gas stations list should appear with the in-memory fake repositories"
        )
    }

    func testTappingAStationOpensItsDetail() {
        let app = XCUIApplication().launchForUITests()

        let row = app.element(id: UITestSupport.stationRow(Self.firstFakeStationID))
        XCTAssertTrue(row.waitForExistence(timeout: 15))
        row.tap()

        XCTAssertTrue(
            app.element(id: UITestSupport.detailStationName).waitForExistence(timeout: 10),
            "tapping a row should navigate to the detail screen through the shared Navigator"
        )
    }
}
