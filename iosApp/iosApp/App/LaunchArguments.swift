import Foundation

/// Launch flags read once at startup.
///
/// UI-test mode swaps the Kotlin repositories for in-memory fakes (`initKoinIosForUiTests`) so
/// XCUITests are deterministic and never touch the network. It is compiled out of release builds
/// entirely, so a shipped app cannot be talked into fake data by an argument.
enum LaunchArguments {

    static let uiTestMode = "-UITestMode"

    static var isUITestMode: Bool {
        #if DEBUG
        ProcessInfo.processInfo.arguments.contains(uiTestMode)
        #else
        false
        #endif
    }
}
