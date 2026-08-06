import SwiftUI

/// Loading placeholder.
///
/// Uses `.redacted(reason: .placeholder)` over the real layout instead of a hand-rolled shimmer:
/// that is the idiomatic iOS path, it keeps the skeleton in sync with the row it stands in for, and
/// it is automatically excluded from accessibility.
struct FuelioSkeletonList: View {

    var rowCount: Int = 6

    var body: some View {
        VStack(spacing: FuelioSpacing.sm) {
            ForEach(0..<rowCount, id: \.self) { _ in
                FuelioCard {
                    VStack(alignment: .leading, spacing: FuelioSpacing.sm) {
                        Text(verbatim: "1.535 €/L").font(.fuelio(.title, weight: .semibold))
                        Text(verbatim: "Placeholder station name").font(.fuelio(.headline))
                        Text(verbatim: "Placeholder address, 41550 Municipality")
                            .font(.fuelio(.caption))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(FuelioSpacing.md)
                }
            }
        }
        .padding(.horizontal, FuelioSpacing.md)
        .padding(.top, FuelioSpacing.sm)
        .redacted(reason: .placeholder)
        .accessibilityHidden(true)
    }
}

/// Detail-screen counterpart: title, map area, price grid and schedule block.
struct FuelioDetailSkeleton: View {

    var body: some View {
        VStack(alignment: .leading, spacing: FuelioSpacing.lg) {
            VStack(alignment: .leading, spacing: FuelioSpacing.xs) {
                Text(verbatim: "Placeholder station").font(.fuelio(.title2, weight: .semibold))
                Text(verbatim: "Placeholder address, 41550 Municipality").font(.fuelio(.callout))
            }
            RoundedRectangle(cornerRadius: FuelioRadius.large)
                .fill(FuelioColors.outline.opacity(0.3))
                .aspectRatio(16.0 / 9.0, contentMode: .fit)
            HStack(spacing: FuelioSpacing.sm) {
                ForEach(0..<2, id: \.self) { _ in
                    FuelioCard {
                        VStack(alignment: .leading, spacing: FuelioSpacing.xxs) {
                            Text(verbatim: "Gasoline 95").font(.fuelio(.caption))
                            Text(verbatim: "1.535 €/L").font(.fuelio(.title, weight: .semibold))
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(FuelioSpacing.md)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(FuelioSpacing.md)
        .redacted(reason: .placeholder)
        .accessibilityHidden(true)
    }
}

#Preview("List skeleton") {
    ScrollView { FuelioSkeletonList() }
}

#Preview("Detail skeleton · Dark") {
    FuelioDetailSkeleton()
        .preferredColorScheme(.dark)
}
