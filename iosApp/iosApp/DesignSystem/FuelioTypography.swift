import SwiftUI
import CorePresentation

/// Inter, sized from `FuelioTypeTokens` (`:core:presentation`) but scaled via iOS Dynamic Type
/// (`relativeTo:`) instead of Material's 15 fixed sizes — this respects the user's system text
/// size setting, which the Android side does not need to (Compose isn't relative-scaled here).
///
/// PostScript name verified against `UIFont.familyNames`/`UIFont.fontNames(forFamilyName:)` once
/// `inter_variable.ttf` / `inter_italic.ttf` are registered via `Info.plist` `UIAppFonts`; falls
/// back to the system font if the name doesn't match (e.g. the variable font registers under a
/// different family name than plain "Inter").
extension Font {
    static func fuelio(_ style: Font.TextStyle, weight: Font.Weight = .regular) -> Font {
        let size = fuelioBaseSize(for: style)
        guard UIFont.familyNames.contains(where: { $0.localizedCaseInsensitiveContains("Inter") }) else {
            return .system(size: size, weight: weight).leading(.standard)
        }
        return .custom("Inter", size: size, relativeTo: style).weight(weight)
    }

    private static func fuelioBaseSize(for style: Font.TextStyle) -> CGFloat {
        switch style {
        case .largeTitle: CGFloat(FuelioTypeTokens.shared.HEADLINE_LARGE_SIZE)
        case .title: CGFloat(FuelioTypeTokens.shared.HEADLINE_SMALL_SIZE)
        case .title2: CGFloat(FuelioTypeTokens.shared.TITLE_LARGE_SIZE)
        case .title3: CGFloat(FuelioTypeTokens.shared.TITLE_MEDIUM_SIZE)
        case .headline: CGFloat(FuelioTypeTokens.shared.TITLE_SMALL_SIZE)
        case .body: CGFloat(FuelioTypeTokens.shared.BODY_LARGE_SIZE)
        case .callout: CGFloat(FuelioTypeTokens.shared.BODY_MEDIUM_SIZE)
        case .subheadline: CGFloat(FuelioTypeTokens.shared.BODY_MEDIUM_SIZE)
        case .footnote: CGFloat(FuelioTypeTokens.shared.LABEL_LARGE_SIZE)
        case .caption: CGFloat(FuelioTypeTokens.shared.LABEL_MEDIUM_SIZE)
        case .caption2: CGFloat(FuelioTypeTokens.shared.LABEL_SMALL_SIZE)
        default: CGFloat(FuelioTypeTokens.shared.BODY_LARGE_SIZE)
        }
    }
}
