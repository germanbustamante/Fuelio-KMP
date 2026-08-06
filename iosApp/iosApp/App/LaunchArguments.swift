import Foundation

/// Test-mode detection, read once at startup.
///
/// In test mode the app starts Koin with `initKoinIosForUiTests()`, which swaps the repositories and
/// the location permission prompt for in-memory fakes. Everything above that boundary — use cases,
/// ViewModels, `Navigator`, analytics — is still the production graph.
///
/// It covers two situations:
/// * XCUITests, which pass `-UITestMode` as a launch argument;
/// * unit tests, which are *hosted by this app*, so `iOSApp.init()` runs before any test does. XCTest
///   sets `XCTestConfigurationFilePath` in that process, which is the standard way to detect it.
///
/// The whole thing is compiled out of release builds, so a shipped app cannot be talked into serving
/// fake data by an argument.
enum LaunchArguments {

    static let uiTestMode = "-UITestMode"

    /// Makes the gas station repository fail so an XCUITest can reach the blocking error state.
    static let uiTestFailureMode = "-UITestFailure"

    static var usesDeterministicData: Bool {
        #if DEBUG
        let processInfo = ProcessInfo.processInfo
        return processInfo.arguments.contains(uiTestMode)
            || processInfo.environment["XCTestConfigurationFilePath"] != nil
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
}
