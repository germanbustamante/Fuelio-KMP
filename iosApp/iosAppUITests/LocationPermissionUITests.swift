import XCTest

/// The UI-test Koin overrides install a `LocationPermissionController` that reports a permanent
/// denial without touching CoreLocation, so the "denied for good" branch is deterministic and no
/// system dialog can block the run.
final class LocationPermissionUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testDetectingLocationWhenPermanentlyDeniedOffersSettings() {
        let app = XCUIApplication().launchForUITests()

        let detect = app.element(id: UITestSupport.detectLocationButton)
        XCTAssertTrue(detect.waitForExistence(timeout: 15))
        detect.tap()

        let settingsButton = app.element(id: UITestSupport.permissionAlertSettings)
        XCTAssertTrue(
            settingsButton.waitForExistence(timeout: 10),
            "a permanent denial should surface an alert offering to open Settings"
        )
    }

    func testDismissingThePermissionAlertDoesNotBringItBack() {
        let app = XCUIApplication().launchForUITests()

        let detect = app.element(id: UITestSupport.detectLocationButton)
        XCTAssertTrue(detect.waitForExistence(timeout: 15))
        detect.tap()

        let settingsButton = app.element(id: UITestSupport.permissionAlertSettings)
        XCTAssertTrue(settingsButton.waitForExistence(timeout: 10))
        app.alerts.buttons.element(boundBy: 1).tap()

        XCTAssertFalse(
            settingsButton.waitForExistence(timeout: 2),
            "dismissing must clear showPermissionDeniedPermanentlySnackbar, or the alert reappears"
        )
    }
}
