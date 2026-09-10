import FirebaseCrashlytics
import CorePresentation

/// Swift half of the shared `CrashReporter`, registered at launch the same way the two tracker
/// bridges are.
///
/// The Kotlin side flattens the `Throwable` before calling `record` — a Kotlin exception does not
/// cross the bridge as anything `Crashlytics` can consume — so this only has to reassemble an
/// `ExceptionModel` from the parts.
class CrashReporterBridge: NativeCrashReporter {

    func setEnabled(enabled: Bool) {
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
    }

    func log(message: String) {
        Crashlytics.crashlytics().log(message)
    }

    func record(name: String, reason: String, stackTrace: [String]) {
        let model = ExceptionModel(name: name, reason: reason)
        // Crashlytics groups non-fatals by their frames, so a Kotlin stack trace has to be mapped
        // onto `StackFrame`s or every Kotlin non-fatal would collapse into one issue.
        model.stackTrace = stackTrace.map { StackFrame(symbol: $0, file: "", line: 0) }
        Crashlytics.crashlytics().record(exceptionModel: model)
    }

    func setCustomKey(key: String, value: String) {
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }
}
