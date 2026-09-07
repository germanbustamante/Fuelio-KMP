import XCTest

/// Deep links enter the app at an inner screen, so the interesting assertion is not just "the detail
/// opened" but "back still leads to the list" — a synthetic back stack is the whole point of the
/// feature, and the one thing a naive `path = [detail]` implementation would get wrong.
///
/// Uses the `-UITestDeepLink` launch argument rather than driving Safari: XCUITest cannot open a URL
/// inside the app's own process, and Safari is slow, network-dependent and gated by a localized
/// system confirmation alert. The launch argument exercises everything the app owns — the pending-URI
/// buffer, `AppRouter.start()` registering the listener, `parseDeepLink`, the synthetic back stack —
/// and leaves only SpringBoard's URL delivery, which is Apple's code, to a manual
/// `xcrun simctl openurl` check.
final class DeepLinkUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testDeepLinkOpensTheStationDetail() {
        let app = XCUIApplication().launchForUITests(
            deepLink: UITestSupport.stationDetailDeepLink(UITestSupport.repsolStationID)
        )

        XCTAssertTrue(app.element(id: UITestSupport.detailStationName).waitForExistence(timeout: 15))
    }

    func testBackFromADeepLinkLandsOnTheStationList() {
        let app = XCUIApplication().launchForUITests(
            deepLink: UITestSupport.stationDetailDeepLink(UITestSupport.repsolStationID)
        )
        let backButton = app.element(id: UITestSupport.detailBackButton)
        XCTAssertTrue(backButton.waitForExistence(timeout: 15))

        backButton.tap()

        XCTAssertTrue(
            wait(app.element(id: UITestSupport.stationsList), satisfies: "exists == true", timeout: 10),
            "the synthetic back stack should leave the list underneath the deep-linked detail"
        )
    }

    /// `getGasStationById` is a local-only Room/`Flow` read, so a link to a station the device has
    /// never cached is an expected business state, not an error — and one the UI cannot otherwise
    /// reach, since the list only offers stations it already has.
    func testDeepLinkToAnUncachedStationShowsNotFound() {
        let app = XCUIApplication().launchForUITests(
            deepLink: UITestSupport.stationDetailDeepLink(UITestSupport.uncachedStationID)
        )

        XCTAssertTrue(app.element(id: UITestSupport.detailNotFound).waitForExistence(timeout: 15))
    }

    func testUnsupportedDeepLinkStaysOnTheList() {
        let app = XCUIApplication().launchForUITests(
            deepLink: "fuelio://station/\(UITestSupport.repsolStationID)/map"
        )

        app.waitForStationList()
        XCTAssertFalse(app.element(id: UITestSupport.detailStationName).exists)
    }
}
