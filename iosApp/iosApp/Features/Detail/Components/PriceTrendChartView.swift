import SwiftUI
import Charts
import CorePresentation

/// `PriceTrendVO` already carries chart-ready points plus `minPrice`/`maxPrice` — no price math
/// happens here, per the "business logic stays in Kotlin" invariant.
struct PriceTrendChartView: View {

    let trend: PriceTrendVO

    var body: some View {
        VStack(alignment: .leading, spacing: FuelioSpacing.sm) {
            Text("Price trend")
                .font(.fuelio(.footnote, weight: .medium))
                .foregroundStyle(.secondary)

            FuelioCard {
                VStack(alignment: .leading, spacing: FuelioSpacing.xs) {
                    Chart(trend.points, id: \.recordedOn) { point in
                        LineMark(
                            x: .value("Date", point.recordedOn.toDate()),
                            y: .value("Price", point.price)
                        )
                        .foregroundStyle(FuelioColors.accent)
                        .interpolationMethod(.monotone)
                    }
                    .chartYScale(domain: trend.minPrice...trend.maxPrice)
                    .chartXAxis(.hidden)
                    .chartYAxis(.hidden)
                    .frame(height: 120)

                    HStack {
                        Text(NumberFormatterKt.formatAsEuros(trend.minPrice))
                        Spacer()
                        Text(NumberFormatterKt.formatAsEuros(trend.maxPrice))
                    }
                    .font(.fuelio(.caption))
                    .foregroundStyle(.secondary)
                }
                .padding(FuelioSpacing.sm)
            }
        }
        .accessibilityIdentifier(A11yID.detailPriceTrendChart)
    }
}

private extension Kotlinx_datetimeLocalDate {
    /// Swift Charts plots `Date`, not a Kotlin `LocalDate` — midnight UTC is precise enough for a
    /// day-granularity axis with no time component to lose.
    func toDate() -> Date {
        Date(timeIntervalSince1970: Double(toEpochDays()) * 86400)
    }
}

#Preview {
    PriceTrendChartView(trend: GasStationDetailFakesKt.fakePriceTrendVO)
        .padding()
}
