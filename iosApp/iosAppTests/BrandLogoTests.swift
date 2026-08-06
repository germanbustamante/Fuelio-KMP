import Testing
@testable import Fuelio
import CorePresentation

@Suite("Brand logo mapping")
struct BrandLogoTests {

    @Test("Derives the asset name from the Kotlin enum, matching the Android drawable naming")
    func derivesAssetName() {
        #expect(BrandLogo.assetName(for: DomainGasStationBrand.repsol) == "logo_repsol")
        #expect(BrandLogo.assetName(for: DomainGasStationBrand.q8) == "logo_q8")
        #expect(BrandLogo.assetName(for: DomainGasStationBrand.ballenoil) == "logo_ballenoil")
    }

    @Test("Has no asset name for an unbranded station")
    func hasNoNameWithoutBrand() {
        #expect(BrandLogo.assetName(for: nil) == nil)
    }

    @Test("Covers all sixteen brands without a Swift switch, so a new brand needs no change here")
    func coversEveryBrand() {
        let names = DomainGasStationBrand.entries.compactMap { BrandLogo.assetName(for: $0) }

        #expect(names.count == DomainGasStationBrand.entries.count)
        #expect(Set(names).count == names.count)
        #expect(names.allSatisfy { $0.hasPrefix("logo_") && $0 == $0.lowercased() })
    }

    @Test("Falls back to an SF Symbol until the brand artwork lands")
    func fallsBackToSymbol() {
        // Brand artwork is still pending design — Android's XML vectors cannot be reused — so no
        // asset is expected to exist yet. This flips to a real check once the assets are added.
        #expect(!BrandLogo.hasAsset(for: DomainGasStationBrand.repsol))
        #expect(BrandLogo.fallbackSymbol == "fuelpump.fill")
    }

    @Test("Resolves the brand off the station name through shared Kotlin logic")
    func resolvesBrandFromStationName() {
        let repsol = FakeGasStationsKt.fakeGasStations.first { $0.name == "REPSOL" }
        let unbranded = FakeGasStationsKt.fakeGasStations.first { $0.name == "NOVAOIL" }

        #expect(repsol?.brand == DomainGasStationBrand.repsol)
        #expect(unbranded?.brand == nil)
    }
}
