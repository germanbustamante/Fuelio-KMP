import SwiftUI
import CorePresentation

/// Price with its per-litre unit, formatted by the **shared** Kotlin formatter.
///
/// `formatAsEuros()` already appends " €/L"; the unit is split back out so it can be typeset smaller
/// without reimplementing number formatting in Foundation (which would let Android and iOS drift).
struct FuelioPriceLabel: View {

    private static let unitSuffix = " €/L"

    let price: Double?
    var isHighlighted: Bool = false

    private var formatted: String? {
        price.map { NumberFormatterKt.formatAsEuros($0) }
    }

    var body: some View {
        HStack(alignment: .lastTextBaseline, spacing: FuelioSpacing.xxs) {
            if let formatted, formatted.hasSuffix(Self.unitSuffix) {
                Text(String(formatted.dropLast(Self.unitSuffix.count)))
                    .font(.fuelio(.title, weight: .semibold))
                    .foregroundStyle(isHighlighted ? FuelioColors.accent : FuelioColors.onSurface)
                Text("€/L")
                    .font(.fuelio(.caption))
                    .foregroundStyle(.secondary)
            } else {
                Text(verbatim: "—")
                    .font(.fuelio(.title, weight: .semibold))
                    .foregroundStyle(.secondary)
            }
        }
    }
}

#Preview("Light") {
    VStack(alignment: .leading, spacing: FuelioSpacing.sm) {
        FuelioPriceLabel(price: 1.535, isHighlighted: true)
        FuelioPriceLabel(price: 1.829)
        FuelioPriceLabel(price: nil)
    }
    .padding(FuelioSpacing.md)
}

#Preview("Dark") {
    FuelioPriceLabel(price: 1.535, isHighlighted: true)
        .padding(FuelioSpacing.md)
        .preferredColorScheme(.dark)
}
