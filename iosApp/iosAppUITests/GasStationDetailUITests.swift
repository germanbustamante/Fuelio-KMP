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

    // The detail "not found" state cannot be reached by tapping through the list — it only offers
    // stations that are already cached — but a deep link to an uncached station id reaches exactly
    // that state, and `DeepLinkUITests.testDeepLinkToAnUncachedStationShowsNotFound` covers it.

    func testStarringFromTheDetailScreenShowsUpInFavorites() {
        // Same source of truth as the list's star — the favourites table, not screen-local state —
        // so a toggle from the detail screen must be visible from the Favorites screen too.
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID)).tap()
        let favoriteButton = app.element(id: UITestSupport.detailFavoriteButton)
        XCTAssertTrue(favoriteButton.waitForExistence(timeout: 10))
        favoriteButton.tap()
        app.element(id: UITestSupport.detailBackButton).tap()

        app.element(id: UITestSupport.favoritesButton).tap()

        XCTAssertTrue(app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID)).waitForExistence(timeout: 10))
    }
}
