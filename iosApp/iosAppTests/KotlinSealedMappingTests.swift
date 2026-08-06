import Testing
@testable import Fuelio
import CorePresentation

/// Guards `Support/KotlinSealed.swift`, the single place where Kotlin sealed types become Swift
/// enums. If Kotlin gains a variant, the mapping falls into its `default` branch and these tests are
/// what turn that into a visible failure instead of a silently wrong screen.
@Suite("Kotlin sealed-type mapping")
struct KotlinSealedMappingTests {

    // MARK: - List content state

    @Test("Maps every list ContentState variant")
    func mapsListContentState() {
        #expect(GasStationsContent(ContentState_Initial.shared) == .initial)
        #expect(GasStationsContent(ContentState_Loading.shared) == .loading)
        #expect(GasStationsContent(ContentState_Empty.shared) == .empty)
        #expect(GasStationsContent(ContentState_Error(message: "boom")) == .failure(message: "boom"))

        let stations = GasStationsFakesKt.fakeGasStationItemVOs
        #expect(GasStationsContent(ContentState_Success(stations: stations)) == .success(stations: stations))
    }

    @Test("Derives the list content state from a real UIState")
    func derivesListContentFromUIState() {
        #expect(GasStationsFakesKt.fakeGasStationsUIStateLoading.content == .loading)
        #expect(GasStationsFakesKt.fakeGasStationsUIStateError.content == .failure(message: "Server error: 403"))

        let loaded = GasStationsFakesKt.fakeGasStationsUIState
        #expect(loaded.content == .success(stations: loaded.gasStations))
    }

    // MARK: - Detail content state

    @Test("Maps every detail ContentState variant")
    func mapsDetailContentState() {
        #expect(GasStationDetailContent(ContentStateLoading.shared) == .loading)
        #expect(GasStationDetailContent(ContentStateNotFound.shared) == .notFound)

        let station = FakeGasStationsKt.fakeGasStations[0]
        let success = ContentStateSuccess(gasStation: station, today: Kotlinx_datetimeDayOfWeek.monday)
        #expect(GasStationDetailContent(success) == .success(station: station, scheduleDays: success.scheduleDays))
    }

    // MARK: - Fuel filter

    @Test("Round-trips every FuelFilter variant", arguments: FuelKind.allCases)
    func roundTripsFuelFilter(kind: FuelKind) {
        #expect(FuelKind(kind.kotlin) == kind)
    }

    @Test("Covers the whole Kotlin FuelFilter hierarchy")
    func coversEveryFuelFilter() {
        #expect(FuelKind(FuelFilterGasoline95.shared) == .gasoline95)
        #expect(FuelKind(FuelFilterGasoline98.shared) == .gasoline98)
        #expect(FuelKind(FuelFilterDiesel.shared) == .diesel)
        #expect(FuelKind(FuelFilterDieselPremium.shared) == .dieselPremium)
        #expect(FuelKind.allCases.count == 4)
    }

    // MARK: - Schedule

    @Test("Maps every ScheduleDayStatus variant")
    func mapsScheduleStatus() {
        #expect(ScheduleStatus(ScheduleDayStatusClosed.shared) == .closed)
        #expect(ScheduleStatus(ScheduleDayStatusAlwaysOpen.shared) == .alwaysOpen)
        #expect(ScheduleStatus(ScheduleDayStatusHours(start: "08:00", end: "22:00")) == .hours(start: "08:00", end: "22:00"))
    }

    @Test("Maps DayOfWeek through its ordinal, since it exports as a KotlinEnum")
    func mapsWeekday() {
        #expect(Weekday(Kotlinx_datetimeDayOfWeek.monday) == .monday)
        #expect(Weekday(Kotlinx_datetimeDayOfWeek.sunday) == .sunday)
        #expect(Weekday.allCases.count == 7)
    }

    @Test("Keeps the shared week order and marks today")
    func keepsSharedWeekOrder() {
        let station = FakeGasStationsKt.fakeGasStations[0]
        let days = GasStationScheduleVOKt.toScheduleDays(station.schedule, today: Kotlinx_datetimeDayOfWeek.thursday)

        #expect(days.map(\.weekday) == Weekday.allCases)
        #expect(days.filter(\.isToday).map(\.weekday) == [.thursday])
        // The fakes are open all week, so every day is the always-open variant.
        #expect(days.allSatisfy { $0.scheduleStatus == .alwaysOpen })
    }
}
