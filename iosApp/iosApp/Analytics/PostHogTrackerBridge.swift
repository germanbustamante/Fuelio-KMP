import PostHog
import CorePresentation

class PostHogTrackerBridge: NativePostHogTracker {

    func logEvent(name: String, params: [String: Any]) {
        PostHogSDK.shared.capture(name, properties: params)
    }

    func logScreen(name: String, params: [String: Any]) {
        PostHogSDK.shared.screen(name, properties: params)
    }

    func logIdentify(distinctId: String, properties: [String: Any]) {
        PostHogSDK.shared.identify(distinctId, userProperties: properties)
        // register, not identify's userProperties, is what rides along on every future event.
        PostHogSDK.shared.register(properties)
    }
}
