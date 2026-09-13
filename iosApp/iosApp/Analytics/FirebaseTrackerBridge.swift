import FirebaseAnalytics
import CorePresentation

class FirebaseTrackerBridge: NativeFirebaseTracker {

    func logEvent(name: String, params: [String: Any]) {
        Analytics.logEvent(name, parameters: params)
    }

    func logIdentify(distinctId: String, properties: [String: Any]) {
        Analytics.setUserID(distinctId)
        for (key, value) in properties {
            Analytics.setUserProperty(String(describing: value), forName: key)
        }
    }
}
