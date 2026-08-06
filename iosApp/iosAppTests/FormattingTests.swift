import Testing
@testable import Fuelio
import CorePresentation

@Suite("Display formatting")
struct FormattingTests {

    @Test("Uses the shared Kotlin formatter for prices and distances")
    func usesSharedFormatters() {
        let item = GasStationsFakesKt.fakeGasStationItemVOs[0]

        // Both `format(digits:)` actuals are locale-aware on purpose, so the assertion is on shape,
        // exactly as `NumberFormatterTest` does on the Kotlin side.
        #expect(item.formattedPrice?.hasSuffix(" €/L") == true)
        #expect(item.formattedPrice == NumberFormatterKt.formatAsEuros(item.price ?? 0))
    }

    @Test("Unwraps the boxed KotlinDouble prices")
    func unwrapsBoxedDoubles() {
        let item = GasStationsFakesKt.fakeGasStationItemVOs[0]

        #expect(item.price == item.getCurrentFuelPrice()?.doubleValue)
        #expect(item.distanceKilometers == nil)
        #expect(item.formattedDistance == nil)
    }

    @Test("Tones down the shouted upstream station names")
    func normalizesStationName() {
        let repsol = FakeGasStationsKt.fakeGasStations.first { $0.name == "REPSOL" }
        let cooperative = FakeGasStationsKt.fakeGasStations.first { $0.name.hasPrefix("S.C.A.") }

        #expect(repsol?.displayName == "Repsol")
        #expect(cooperative?.displayName == "S.c.a. ntra. sra. de la fuensanta")
    }

    @Test("Localizes every fuel kind", arguments: FuelKind.allCases)
    func localizesFuelKinds(kind: FuelKind) {
        #expect(!kind.localizedTitle.isEmpty)
        #expect(kind.localizedTitle != kind.rawValue)
    }

    @Test("Localizes every weekday", arguments: Weekday.allCases)
    func localizesWeekdays(day: Weekday) {
        #expect(!day.localizedName.isEmpty)
    }
}
