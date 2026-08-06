import CorePresentation

/// Handle for an in-flight state subscription.
///
/// The Kotlin `FlowSubscription` has an `internal` constructor and is therefore impossible to build
/// from Swift, so a backend protocol returning it directly could never be implemented by a test
/// double. This protocol is the seam; `FlowSubscription` already satisfies it as-is.
protocol StateSubscription: AnyObject {
    var isCancelled: Bool { get }
    func cancel()
}

extension FlowSubscription: StateSubscription {}
