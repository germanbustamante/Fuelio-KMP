import SwiftUI
import CorePresentation

/// Weekly opening hours with today emphasized.
///
/// The rows come from the Kotlin `ContentState.Success.scheduleDays`, so the week order, the
/// "is this today" flag and the closed/24h/range classification are all shared with Android. Only the
/// day names are localized here, off `DayOfWeek`, never hard-coded in English.
struct StationScheduleSection: View {

    let days: [ScheduleDayVO]

    var body: some View {
        VStack(alignment: .leading, spacing: FuelioSpacing.sm) {
            Text("Schedule")
                .font(.fuelio(.footnote, weight: .medium))
                .foregroundStyle(.secondary)

            FuelioCard {
                VStack(spacing: 0) {
                    ForEach(Array(days.enumerated()), id: \.offset) { index, day in
                        row(for: day)
                        if index < days.count - 1 {
                            Divider().padding(.leading, FuelioSpacing.md)
                        }
                    }
                }
            }
        }
        .accessibilityIdentifier(A11yID.detailScheduleSection)
    }

    private func row(for day: ScheduleDayVO) -> some View {
        // `ViewThatFits` keeps day and hours side by side while they fit and stacks them once the
        // text grows, without hard-coding a size threshold.
        ViewThatFits(in: .horizontal) {
            HStack {
                Text(dayTitle(for: day))
                Spacer(minLength: FuelioSpacing.sm)
                Text(hoursTitle(for: day))
            }
            VStack(alignment: .leading, spacing: FuelioSpacing.xxs) {
                Text(dayTitle(for: day))
                Text(hoursTitle(for: day))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .font(day.isToday ? .fuelio(.headline) : .fuelio(.callout))
        .foregroundStyle(day.isToday ? FuelioColors.onSurface : Color.secondary)
        .padding(.horizontal, FuelioSpacing.md)
        .padding(.vertical, FuelioSpacing.sm)
        .accessibilityElement(children: .combine)
    }

    private func dayTitle(for day: ScheduleDayVO) -> String {
        let name = day.weekday.localizedName
        return day.isToday ? name + String(localized: " · today") : name
    }

    private func hoursTitle(for day: ScheduleDayVO) -> String {
        switch day.scheduleStatus {
        case .closed: String(localized: "Closed")
        case .alwaysOpen: String(localized: "Open 24h")
        case .hours(let start, let end): "\(start)–\(end)"
        }
    }
}

#Preview("Light") {
    let station = FakeGasStationsKt.fakeGasStations[0]
    return StationScheduleSection(
        days: GasStationScheduleVOKt.toScheduleDays(station.schedule, today: Kotlinx_datetimeDayOfWeek.wednesday)
    )
    .padding(FuelioSpacing.md)
}

#Preview("Dark") {
    let station = FakeGasStationsKt.fakeGasStations[0]
    return StationScheduleSection(
        days: GasStationScheduleVOKt.toScheduleDays(station.schedule, today: Kotlinx_datetimeDayOfWeek.sunday)
    )
    .padding(FuelioSpacing.md)
    .preferredColorScheme(.dark)
}
