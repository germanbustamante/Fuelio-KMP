import FirebaseAnalytics
import ComposeApp

class FirebaseTrackerBridge: NativeFirebaseTracker {

    func logEvent(name: String, params: [String: Any]) {
        Analytics.logEvent(name, parameters: params)
    }
}
