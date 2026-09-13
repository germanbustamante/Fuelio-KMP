import XCTest

/// Android mirror: `OnboardingTest.kt`.
final class OnboardingUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testAppLaunchesIntoOnboardingWhenNotCompletedYet() {
        let app = XCUIApplication().launchForUITests(showOnboarding: true)

        XCTAssertTrue(app.element(id: UITestSupport.onboardingScreen).waitForExistence(timeout: 10))
        XCTAssertFalse(app.element(id: UITestSupport.stationsList).exists)
    }

    func testFinishingOnboardingRevealsTheStationList() {
        let app = XCUIApplication().launchForUITests(showOnboarding: true)
        XCTAssertTrue(app.element(id: UITestSupport.onboardingScreen).waitForExistence(timeout: 10))

        let nextButton = app.element(id: UITestSupport.onboardingNextButton)
        XCTAssertTrue(nextButton.waitForExistence(timeout: 10))
        nextButton.tap()

        let allowLocationButton = app.element(id: UITestSupport.onboardingAllowLocationButton)
        XCTAssertTrue(allowLocationButton.waitForExistence(timeout: 10))
        allowLocationButton.tap()

        let finishButton = app.element(id: UITestSupport.onboardingFinishButton)
        XCTAssertTrue(finishButton.waitForExistence(timeout: 10))
        finishButton.tap()

        app.waitForStationList()
        XCTAssertFalse(app.element(id: UITestSupport.onboardingScreen).exists)
    }

    func testSkippingFromTheWelcomeStepRevealsTheStationList() {
        let app = XCUIApplication().launchForUITests(showOnboarding: true)
        XCTAssertTrue(app.element(id: UITestSupport.onboardingScreen).waitForExistence(timeout: 10))

        let skipButton = app.element(id: UITestSupport.onboardingSkipButton)
        XCTAssertTrue(skipButton.waitForExistence(timeout: 10))
        skipButton.tap()

        app.waitForStationList()
    }

    func testSkippingLocationPermissionStillReachesTheDefaultFuelStep() {
        let app = XCUIApplication().launchForUITests(showOnboarding: true)
        XCTAssertTrue(app.element(id: UITestSupport.onboardingScreen).waitForExistence(timeout: 10))

        let nextButton = app.element(id: UITestSupport.onboardingNextButton)
        XCTAssertTrue(nextButton.waitForExistence(timeout: 10))
        nextButton.tap()

        let skipButton = app.element(id: UITestSupport.onboardingSkipButton)
        XCTAssertTrue(skipButton.waitForExistence(timeout: 10))
        skipButton.tap()

        XCTAssertTrue(app.element(id: UITestSupport.onboardingFuelPicker).waitForExistence(timeout: 10))
    }
}
