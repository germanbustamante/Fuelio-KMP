import Foundation

/// Test-mode detection, read once at startup.
///
/// In test mode the app starts Koin with `initKoinIosForUiTests()`, which swaps the repositories and
/// the location permission prompt for in-memory fakes. Everything above that boundary — use cases,
/// ViewModels, `Navigator`, analytics — is still the production graph.
///
/// It covers three situations:
/// * XCUITests, which pass `-UITestMode` as a launch argument;
/// * unit tests, which are *hosted by this app*, so `iOSApp.init()` runs before any test does. XCTest
///   sets `XCTestConfigurationFilePath` in that process, which is the standard way to detect it;
/// * SwiftUI Previews, which also launch the full `@main App` process (Xcode 16+ app-hosted previews)
///   before swapping in the previewed view — real network/Room calls would just hang or fail there.
///
/// The whole thing is compiled out of release builds, so a shipped app cannot be talked into serving
/// fake data by an argument.
enum LaunchArguments {

    static let uiTestMode = "-UITestMode"

    /// Makes the gas station repository fail so an XCUITest can reach the blocking error state.
    static let uiTestFailureMode = "-UITestFailure"

    /// Feeds a deep link into `ExternalUriHandler` at startup so an XCUITest can exercise the
    /// external-URI path. XCUITest cannot open a URL inside the app's own process, and driving
    /// Safari instead is slow, network-dependent and gated by a localized system confirmation alert
    /// — this reproduces every step the app owns (buffering, `AppRouter.start()` registering the
    /// listener, `parseDeepLink`, the synthetic back stack) and leaves only SpringBoard's URL
    /// delivery, which is Apple's code, to a manual `xcrun simctl openurl` check.
    static let uiTestDeepLink = "-UITestDeepLink"

    static var usesDeterministicData: Bool {
        #if DEBUG
        let processInfo = ProcessInfo.processInfo
        return processInfo.arguments.contains(uiTestMode)
            || processInfo.environment["XCTestConfigurationFilePath"] != nil
            || processInfo.environment["XCODE_RUNNING_FOR_PREVIEWS"] == "1"
        #else
        return false
        #endif
    }

    static var simulatesStationFailure: Bool {
        #if DEBUG
        ProcessInfo.processInfo.arguments.contains(uiTestFailureMode)
        #else
        false
        #endif
    }

    static var deepLinkUri: String? {
        #if DEBUG
        let arguments = ProcessInfo.processInfo.arguments
        guard let flagIndex = arguments.firstIndex(of: uiTestDeepLink),
              arguments.indices.contains(flagIndex + 1) else { return nil }
        return arguments[flagIndex + 1]
        #else
        return nil
        #endif
    }
}
