import XCTest

final class FavoritesUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testFavoritesStartsEmpty() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        app.element(id: UITestSupport.favoritesButton).tap()

        XCTAssertTrue(app.element(id: UITestSupport.favoritesEmpty).waitForExistence(timeout: 10))
    }

    func testAStationStarredOnTheListShowsUpInFavorites() {
        // The whole point of the Room-backed favourites: the star survives leaving the list, because
        // the list's state is no longer the source of truth.
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        app.element(id: UITestSupport.favoriteButton(UITestSupport.repsolStationID)).tap()
        app.element(id: UITestSupport.favoritesButton).tap()

        XCTAssertTrue(
            app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID)).waitForExistence(timeout: 10)
        )
    }

    func testBackReturnsToTheList() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        app.element(id: UITestSupport.favoritesButton).tap()
        let backButton = app.element(id: UITestSupport.favoritesBackButton)
        XCTAssertTrue(backButton.waitForExistence(timeout: 10))

        backButton.tap()

        // Back goes through the ViewModel and the shared `Navigator`, not by popping the stack.
        XCTAssertTrue(
            wait(app.element(id: UITestSupport.stationsList), satisfies: "exists == true", timeout: 10),
            "onBackTapped() should navigate back through the shared Navigator"
        )
    }
}
