import SwiftUI
import FirebaseCore
import PostHog
import ComposeApp

@main
struct iOSApp: App {

    init() {
        FirebaseApp.configure()
        FirebaseTracker_iosKt.registerNativeFirebaseTracker(tracker: FirebaseTrackerBridge())

        let postHogApiKey = AnalyticsSecrets.shared.POSTHOG_API_KEY
        if !postHogApiKey.isEmpty {
            let config = PostHogConfig(apiKey: postHogApiKey, host: "https://eu.i.posthog.com")
            PostHogSDK.shared.setup(config)
            PostHogTracker_iosKt.registerNativePostHogTracker(tracker: PostHogTrackerBridge())
        }

        KoinInitIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
