import XCTest

/// Walks the real screens and checks that anything interactive is announceable.
///
/// An unlabelled control is invisible to VoiceOver, and that is the kind of regression that never
/// shows up in a screenshot.
final class AccessibilityUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testEveryInteractiveElementOnTheListIsLabelled() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        assertAllLabelled(app.buttons, context: "list buttons")
    }

    func testStationRowsReadAsASingleSentence() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()

        let row = app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID))
        XCTAssertTrue(row.waitForExistence(timeout: 10))

        // `.accessibilityElement(children: .combine)` means one element carrying the whole sentence:
        // name, address, open/closed, fuel and price — not six separate announcements.
        let label = row.label
        XCTAssertFalse(label.isEmpty)
        XCTAssertTrue(label.contains(","), "the row label should be a composed sentence, got \"\(label)\"")
        XCTAssertGreaterThan(label.count, 30, "the row label looks truncated: \"\(label)\"")
    }

    func testEveryInteractiveElementOnTheDetailIsLabelled() {
        let app = XCUIApplication().launchForUITests()
        app.waitForStationList()
        app.element(id: UITestSupport.stationRow(UITestSupport.repsolStationID)).tap()
        XCTAssertTrue(app.element(id: UITestSupport.detailStationName).waitForExistence(timeout: 10))

        assertAllLabelled(app.buttons, context: "detail buttons")
    }

    private func assertAllLabelled(
        _ query: XCUIElementQuery,
        context: String,
        file: StaticString = #filePath,
        line: UInt = #line
    ) {
        let elements = query.allElementsBoundByIndex.filter(\.isHittable)
        XCTAssertFalse(elements.isEmpty, "no hittable elements found for \(context)", file: file, line: line)

        for element in elements {
            let describedByLabel = !element.label.trimmingCharacters(in: .whitespaces).isEmpty
            XCTAssertTrue(
                describedByLabel,
                "unlabelled \(context) element with identifier \"\(element.identifier)\"",
                file: file,
                line: line
            )
        }
    }
}
