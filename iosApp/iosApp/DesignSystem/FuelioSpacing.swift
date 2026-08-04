import SwiftUI
import CorePresentation

/// Shared spacing scale, mirroring `FuelioSpacingTokens` (`:core:presentation`).
enum FuelioSpacing {
    static let xxs = CGFloat(FuelioSpacingTokens.shared.XXS)
    static let xs = CGFloat(FuelioSpacingTokens.shared.XS)
    static let sm = CGFloat(FuelioSpacingTokens.shared.SM)
    static let md = CGFloat(FuelioSpacingTokens.shared.MD)
    static let lg = CGFloat(FuelioSpacingTokens.shared.LG)
    static let xl = CGFloat(FuelioSpacingTokens.shared.XL)
    static let xxl = CGFloat(FuelioSpacingTokens.shared.XXL)
    static let xxxl = CGFloat(FuelioSpacingTokens.shared.XXXL)
}
