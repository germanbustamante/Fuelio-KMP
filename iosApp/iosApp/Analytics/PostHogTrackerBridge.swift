import PostHog
import ComposeApp

class PostHogTrackerBridge: NativePostHogTracker {

    func logEvent(name: String, params: [String: Any]) {
        PostHogSDK.shared.capture(name, properties: params)
    }

    func logScreen(name: String, params: [String: Any]) {
        PostHogSDK.shared.screen(name, properties: params)
    }
}
