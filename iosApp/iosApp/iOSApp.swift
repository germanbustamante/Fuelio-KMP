import SwiftUI
import FirebaseCore
import ComposeApp

@main
struct iOSApp: App {

    init() {
        FirebaseApp.configure()
        FirebaseTracker_iosKt.registerNativeFirebaseTracker(tracker: FirebaseTrackerBridge())
        KoinInitIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
