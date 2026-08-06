import XCTest

/// XCUITest stays on XCTest — Swift Testing does not host UI automation.
final class LaunchUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testAppLaunchesAndShowsTheStationList() {
        let app = XCUIApplication()
        app.launchArguments = [UITestSupport.uiTestMode]
        app.launch()

        XCTAssertEqual(app.state, .runningForeground)
        XCTAssertTrue(
            app.collectionViews[UITestSupport.stationsList].waitForExistence(timeout: 10),
            "the gas stations list should appear with the in-memory fake repositories"
        )
    }
}
