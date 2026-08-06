import Testing
@testable import Fuelio
import CorePresentation

/// Proves the unit-test target is wired up and can reach both the app module and the Kotlin
/// framework. Real coverage lives in the suites added alongside each feature.
@Suite("Kotlin framework reachability")
struct BridgeSmokeTests {

    @Test("Shared fakes are exported to Swift")
    func sharedFakesAreExported() {
        #expect(!GasStationsFakesKt.fakeGasStationItemVOs.isEmpty)
        #expect(!GasStationsFakesKt.fakeProvinces.isEmpty)
    }

    /// Both `format(digits:)` actuals are deliberately locale-aware (`Locale.getDefault()` on
    /// Android, `NSNumberFormatter`'s current locale on iOS), so the decimal separator depends on
    /// the device — assert the shape, not a literal, exactly as `NumberFormatterTest` does in Kotlin.
    @Test("Shared number formatting is used instead of Foundation")
    func sharedNumberFormatting() {
        let price = NumberFormatterKt.formatAsEuros(1.535)
        let priceDecimals = price.dropLast(4).suffix(3).filter(\.isNumber).count
        #expect(price.hasSuffix(" €/L"))
        #expect(priceDecimals == 3)

        let distance = NumberFormatterKt.formatAsKilometers(2.4)
        let distanceDecimals = distance.dropLast(3).suffix(1).filter(\.isNumber).count
        #expect(distance.hasSuffix(" km"))
        #expect(distanceDecimals == 1)
    }
}
