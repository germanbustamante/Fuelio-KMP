import SwiftUI
import CorePresentation

/// One station in the list.
///
/// Everything shown here is already computed by the Kotlin ViewModel — the selected fuel's price,
/// open/closed, distance and which station is cheapest. This view formats and lays it out, nothing
/// more.
struct GasStationRow: View {

    let item: GasStationItemVO
    let isFavorite: Bool
    let onToggleFavorite: () -> Void

    private var station: DomainGasStationBO { item.station }

    var body: some View {
        FuelioCard {
            VStack(alignment: .leading, spacing: FuelioSpacing.sm) {
                header
                if item.isCheapest {
                    FuelioBadge("Cheapest", systemImage: "tag.fill", tone: .accent)
                }
                identity
                footer
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(FuelioSpacing.md)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel(accessibilityLabel)
        .accessibilityIdentifier(A11yID.stationRow(station.id))
    }

    private var header: some View {
        HStack(alignment: .top) {
            FuelioPriceLabel(price: item.price, isHighlighted: item.isCheapest)
                .accessibilityIdentifier(A11yID.stationPrice(station.id))

            Spacer(minLength: FuelioSpacing.sm)

            HStack(spacing: FuelioSpacing.xs) {
                FuelioBadge(
                    item.isOpen ? "Open" : "Closed",
                    systemImage: "clock",
                    tone: item.isOpen ? .success : .danger
                )
                favoriteButton
            }
        }
    }

    private var favoriteButton: some View {
        Button(action: onToggleFavorite) {
            Image(systemName: isFavorite ? "star.fill" : "star")
                .foregroundStyle(isFavorite ? FuelioColors.accent : Color.secondary)
        }
        // `.borderless` keeps the star independently tappable inside a row that is itself tappable.
        .buttonStyle(.borderless)
        .accessibilityIdentifier(A11yID.favoriteButton(station.id))
        .accessibilityLabel(isFavorite ? "Remove from favorites" : "Add to favorites")
    }

    private var identity: some View {
        HStack(spacing: FuelioSpacing.sm) {
            BrandLogoView(brand: station.brand, size: FuelioSpacing.lg)
            Text(station.displayName)
                .font(.fuelio(.headline))
                .lineLimit(1)
        }
    }

    private var footer: some View {
        HStack(alignment: .firstTextBaseline, spacing: FuelioSpacing.sm) {
            Text(station.getFullDirection())
                .font(.fuelio(.caption))
                .foregroundStyle(.secondary)
                .lineLimit(1)

            Spacer(minLength: 0)

            if let formattedDistance = item.formattedDistance {
                Label(formattedDistance, systemImage: "location")
                    .font(.fuelio(.caption2))
                    .foregroundStyle(.secondary)
                    .labelStyle(.titleAndIcon)
            }
        }
    }

    /// Reads as one sentence instead of six disconnected fields, mirroring the Android
    /// `contentDescription`.
    private var accessibilityLabel: String {
        var parts: [String] = [station.displayName, station.getFullDirection()]
        parts.append(item.isOpen ? String(localized: "Open") : String(localized: "Closed"))
        if let formattedDistance = item.formattedDistance {
            parts.append(String(localized: "at \(formattedDistance)"))
        }
        let fuel = FuelKind(item.fuelFilter).localizedTitle
        let price = item.formattedPrice ?? String(localized: "Price unavailable")
        parts.append("\(fuel): \(price)")
        if item.isCheapest {
            parts.append(String(localized: "cheapest station"))
        }
        if isFavorite {
            parts.append(String(localized: "Favorite"))
        }
        return parts.joined(separator: ", ")
    }
}

#Preview("Light") {
    VStack(spacing: FuelioSpacing.md) {
        GasStationRow(item: GasStationsFakesKt.fakeGasStationItemVOs[0], isFavorite: true, onToggleFavorite: {})
        GasStationRow(item: GasStationsFakesKt.fakeGasStationItemVOs[1], isFavorite: false, onToggleFavorite: {})
    }
    .padding(FuelioSpacing.md)
}

#Preview("Dark") {
    GasStationRow(item: GasStationsFakesKt.fakeGasStationItemVOs[0], isFavorite: false, onToggleFavorite: {})
        .padding(FuelioSpacing.md)
        .preferredColorScheme(.dark)
}
