import CorePresentation

extension AppUIState {

    /// `hasCompletedOnboarding` crosses the bridge boxed (`KotlinBoolean?`) since it is an optional
    /// Kotlin primitive — same reasoning as `GasStationItemVO.price` in `GasStationFormatting.swift`.
    /// Named differently from the boxed property (rather than shadowing it) since Swift cannot
    /// redeclare a member with the same name as one already exported from the Kotlin type.
    var onboardingCompletionState: Bool? { hasCompletedOnboarding?.boolValue }
}
