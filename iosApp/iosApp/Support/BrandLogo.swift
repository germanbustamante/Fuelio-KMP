import SwiftUI
import CorePresentation

/// Maps `GasStationBrand` to an asset name.
///
/// Android's brand logos are XML vector drawables, which are unusable on iOS, so **the real assets
/// are still pending design** — until they land, every brand falls back to an SF Symbol. Deriving the
/// name from the Kotlin enum's `name` rather than a Swift `switch` means adding a 17th brand needs no
/// change here; only the asset itself.
enum BrandLogo {

    static let fallbackSymbol = "fuelpump.fill"

    /// e.g. `REPSOL` -> `logo_repsol`, matching the Android drawable naming.
    static func assetName(for brand: DomainGasStationBrand?) -> String? {
        guard let brand else { return nil }
        return "logo_\(brand.name.lowercased())"
    }

    static func hasAsset(for brand: DomainGasStationBrand?) -> Bool {
        guard let name = assetName(for: brand) else { return false }
        return UIImage(named: name) != nil
    }
}

struct BrandLogoView: View {

    let brand: DomainGasStationBrand?
    var size: CGFloat = FuelioSpacing.lg

    var body: some View {
        Group {
            if let name = BrandLogo.assetName(for: brand), BrandLogo.hasAsset(for: brand) {
                Image(name)
                    .resizable()
                    .scaledToFit()
            } else {
                Image(systemName: BrandLogo.fallbackSymbol)
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(FuelioColors.accent)
                    .padding(FuelioSpacing.xxs)
            }
        }
        .frame(width: size, height: size)
        .accessibilityHidden(true)
    }
}

#Preview("Light") {
    HStack(spacing: FuelioSpacing.md) {
        BrandLogoView(brand: DomainGasStationBrand.repsol)
        BrandLogoView(brand: DomainGasStationBrand.cepsa)
        BrandLogoView(brand: nil)
    }
    .padding(FuelioSpacing.md)
}

#Preview("Dark") {
    BrandLogoView(brand: DomainGasStationBrand.shell)
        .padding(FuelioSpacing.md)
        .preferredColorScheme(.dark)
}
