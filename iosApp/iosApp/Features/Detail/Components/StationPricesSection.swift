import SwiftUI
import CorePresentation

/// The four fuel prices in a two-column grid, with the cheapest of the four highlighted.
///
/// "Cheapest of these four" is presentation-only — it compares the numbers already on screen and has
/// nothing to do with `GasStationItemVO.isCheapest`, which the ViewModel computes *across stations*.
/// Android does the same comparison in its Composable, so both platforms keep it in the view layer.
struct StationPricesSection: View {

    let station: DomainGasStationBO

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private struct Entry: Identifiable {
        let id: String
        let title: LocalizedStringKey
        let price: Double?
    }

    private var entries: [Entry] {
        [
            Entry(id: FuelKind.gasoline95.rawValue, title: FuelKind.gasoline95.title, price: station.gasolinePrice95?.doubleValue),
            Entry(id: FuelKind.gasoline98.rawValue, title: FuelKind.gasoline98.title, price: station.gasolinePrice98?.doubleValue),
            Entry(id: FuelKind.diesel.rawValue, title: FuelKind.diesel.title, price: station.dieselPrice?.doubleValue),
            Entry(id: FuelKind.dieselPremium.rawValue, title: FuelKind.dieselPremium.title, price: station.dieselPremiumPrice?.doubleValue),
        ]
    }

    private var cheapest: Double? { entries.compactMap(\.price).min() }

    private var columns: [GridItem] {
        dynamicTypeSize.isAccessibilitySize
            ? [GridItem(.flexible())]
            : [GridItem(.flexible(), spacing: FuelioSpacing.sm), GridItem(.flexible())]
    }

    var body: some View {
        VStack(alignment: .leading, spacing: FuelioSpacing.sm) {
            Text("Fuel prices")
                .font(.fuelio(.footnote, weight: .medium))
                .foregroundStyle(.secondary)

            // Two columns cannot fit a price at accessibility sizes, so the grid collapses to one.
            LazyVGrid(columns: columns, spacing: FuelioSpacing.sm) {
                ForEach(entries) { entry in
                    FuelioCard {
                        VStack(alignment: .leading, spacing: FuelioSpacing.xxs) {
                            Text(entry.title)
                                .font(.fuelio(.caption))
                                .foregroundStyle(.secondary)
                            FuelioPriceLabel(
                                price: entry.price,
                                isHighlighted: entry.price != nil && entry.price == cheapest
                            )
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(FuelioSpacing.md)
                    }
                    .accessibilityElement(children: .combine)
                }
            }
        }
        .accessibilityIdentifier(A11yID.detailPricesSection)
    }
}

#Preview("Light") {
    StationPricesSection(station: FakeGasStationsKt.fakeGasStations[9])
        .padding(FuelioSpacing.md)
}

#Preview("Dark") {
    StationPricesSection(station: FakeGasStationsKt.fakeGasStations[9])
        .padding(FuelioSpacing.md)
        .preferredColorScheme(.dark)
}

#Preview("Accessibility XXXL") {
    ScrollView {
        StationPricesSection(station: FakeGasStationsKt.fakeGasStations[9])
            .padding(FuelioSpacing.md)
    }
    .dynamicTypeSize(.accessibility3)
}
