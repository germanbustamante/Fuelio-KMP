import PostHog
import CorePresentation

class FeatureFlagSourceBridge: NativeFeatureFlagSource {

    func reload(onLoaded: @escaping () -> Void) {
        PostHogSDK.shared.reloadFeatureFlags {
            onLoaded()
        }
    }

    // posthog-ios's `isFeatureEnabled` has no default-value parameter (unlike posthog-android's),
    // so an unknown key falls back to PostHog's own `false` rather than the caller's `default` —
    // acceptable today since no real flag exists yet (see FeatureFlags.kt), but worth remembering
    // once `price_trend_chart` actually ships and needs to default to something other than off.
    func isFeatureEnabled(key: String, default: Bool) -> Bool {
        PostHogSDK.shared.isFeatureEnabled(key)
    }
}
