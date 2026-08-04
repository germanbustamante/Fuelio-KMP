import SwiftUI
import CorePresentation

/// Shared corner radius scale, mirroring `FuelioRadiusTokens` (`:core:presentation`).
/// SwiftUI has no `Shape` token concept like Material — these are plain radii, used with
/// `RoundedRectangle(cornerRadius:)` / `.clipShape`.
enum FuelioRadius {
    static let extraSmall = CGFloat(FuelioRadiusTokens.shared.EXTRA_SMALL)
    static let small = CGFloat(FuelioRadiusTokens.shared.SMALL)
    static let medium = CGFloat(FuelioRadiusTokens.shared.MEDIUM)
    static let large = CGFloat(FuelioRadiusTokens.shared.LARGE)
    static let extraLarge = CGFloat(FuelioRadiusTokens.shared.EXTRA_LARGE)
}
