import SwiftUI
import FirebaseCore
import PostHog
import CorePresentation

@main
struct iOSApp: App {

    init() {
        // SwiftUI Previews (Xcode 16+ app-hosted previews) launch this full `@main App` process under
        // a JIT executor. Firebase/PostHog's static libraries do heavy method swizzling and install
        // signal/mach exception handlers (Crashlytics) that reliably SIGBUS under that JIT host, so
        // they're skipped entirely there — Koin still initializes below, with fakes.
        let isRunningForPreviews = ProcessInfo.processInfo.environment["XCODE_RUNNING_FOR_PREVIEWS"] == "1"

        if !isRunningForPreviews {
            FirebaseApp.configure()
            FirebaseTracker_iosKt.registerNativeFirebaseTracker(tracker: FirebaseTrackerBridge())
            CrashReporter_iosKt.registerNativeCrashReporter(reporter: CrashReporterBridge())

            let postHogApiKey = AnalyticsSecrets.shared.POSTHOG_API_KEY
            if !postHogApiKey.isEmpty {
                let config = PostHogConfig(apiKey: postHogApiKey, host: "https://eu.i.posthog.com")
                PostHogSDK.shared.setup(config)
                PostHogTracker_iosKt.registerNativePostHogTracker(tracker: PostHogTrackerBridge())
            }
        }

        if LaunchArguments.usesDeterministicData {
            KoinInitIosKt.doInitKoinIosForUiTests(simulateStationFailure: LaunchArguments.simulatesStationFailure)
        } else {
            KoinInitIosKt.doInitKoinIos()
        }

        // Buffered by `ExternalUriHandler` until `AppRouter.start()` registers its listener — the
        // exact same path a real cold-launch `.onOpenURL` takes.
        if let deepLinkUri = LaunchArguments.deepLinkUri {
            ExternalUriHandler.shared.onNewUri(uri: deepLinkUri)
        }
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .onOpenURL { url in
                    ExternalUriHandler.shared.onNewUri(uri: url.absoluteString)
                }
        }
    }
}
