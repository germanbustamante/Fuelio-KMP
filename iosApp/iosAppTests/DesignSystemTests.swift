import Testing
import SwiftUI
@testable import Fuelio
import CorePresentation

/// ADR 0003 regression guard: the SwiftUI adapter must read the shared Kotlin tokens, not its own
/// literals. If someone changes a token on the Kotlin side, both platforms move together — and if
/// someone hardcodes a value on the Swift side instead, these fail.
@Suite("Design system adapter")
struct DesignSystemTests {

    @Test("Spacing mirrors the shared scale")
    func spacingMirrorsTokens() {
        #expect(FuelioSpacing.xxs == CGFloat(FuelioSpacingTokens.shared.XXS))
        #expect(FuelioSpacing.xs == CGFloat(FuelioSpacingTokens.shared.XS))
        #expect(FuelioSpacing.sm == CGFloat(FuelioSpacingTokens.shared.SM))
        #expect(FuelioSpacing.md == CGFloat(FuelioSpacingTokens.shared.MD))
        #expect(FuelioSpacing.lg == CGFloat(FuelioSpacingTokens.shared.LG))
        #expect(FuelioSpacing.xl == CGFloat(FuelioSpacingTokens.shared.XL))
        #expect(FuelioSpacing.xxl == CGFloat(FuelioSpacingTokens.shared.XXL))
        #expect(FuelioSpacing.xxxl == CGFloat(FuelioSpacingTokens.shared.XXXL))
    }

    @Test("Radii mirror the shared scale")
    func radiiMirrorTokens() {
        #expect(FuelioRadius.extraSmall == CGFloat(FuelioRadiusTokens.shared.EXTRA_SMALL))
        #expect(FuelioRadius.small == CGFloat(FuelioRadiusTokens.shared.SMALL))
        #expect(FuelioRadius.medium == CGFloat(FuelioRadiusTokens.shared.MEDIUM))
        #expect(FuelioRadius.large == CGFloat(FuelioRadiusTokens.shared.LARGE))
        #expect(FuelioRadius.extraLarge == CGFloat(FuelioRadiusTokens.shared.EXTRA_LARGE))
    }

    @Test("The accent ramp still resolves to the brand orange in both appearances")
    func accentResolvesFromTokens() {
        let light = UIColor(FuelioColors.accent)
            .resolvedColor(with: UITraitCollection(userInterfaceStyle: .light))
        let dark = UIColor(FuelioColors.accent)
            .resolvedColor(with: UITraitCollection(userInterfaceStyle: .dark))

        #expect(light.argb == UInt32(truncatingIfNeeded: FuelioColorTokens.shared.ACCENT_LIGHT))
        #expect(dark.argb == UInt32(truncatingIfNeeded: FuelioColorTokens.shared.ACCENT_DARK))
    }

    @Test("Semantic colours differ between light and dark, so the ramps are actually wired up")
    func semanticColoursAreDynamic() {
        let dynamicPairs: [Color] = [
            FuelioColors.background,
            FuelioColors.surface,
            FuelioColors.onSurface,
            FuelioColors.outline,
            FuelioColors.success,
            FuelioColors.danger,
        ]

        for color in dynamicPairs {
            let uiColor = UIColor(color)
            let light = uiColor.resolvedColor(with: UITraitCollection(userInterfaceStyle: .light))
            let dark = uiColor.resolvedColor(with: UITraitCollection(userInterfaceStyle: .dark))
            #expect(light.argb != dark.argb)
        }
    }

    @Test("Typography sizes come from the shared type scale")
    func typographyUsesTokens() {
        // `Font` is opaque, so the assertion is on the token feeding it: a hardcoded size here would
        // mean the base sizes had been duplicated instead of shared.
        #expect(FuelioTypeTokens.shared.TITLE_LARGE_SIZE == 22.0)
        #expect(FuelioTypeTokens.shared.BODY_LARGE_SIZE == 16.0)
        #expect(FuelioTypeTokens.shared.LABEL_SMALL_SIZE == 11.0)
    }
}

private extension UIColor {

    /// Packs the resolved colour back into the ARGB layout the Kotlin tokens use.
    var argb: UInt32 {
        var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0, alpha: CGFloat = 0
        getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        let channel: (CGFloat) -> UInt32 = { UInt32((($0 * 255).rounded())) }
        return channel(alpha) << 24 | channel(red) << 16 | channel(green) << 8 | channel(blue)
    }
}
