import XCTest

/// XCUITest stays on XCTest — Swift Testing does not host UI automation.
final class LaunchUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testAppLaunches() {
        let app = XCUIApplication()
        app.launch()

        XCTAssertEqual(app.state, .runningForeground)
    }
}
