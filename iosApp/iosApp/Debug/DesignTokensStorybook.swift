import SwiftUI
import CorePresentation

/// Design-token storybook: proves the shared tokens (`:core:presentation`) resolve correctly on iOS
/// and gives a quick visual diff against the Android app — ADR 0003 asks for it to stay.
///
/// It is no longer the app root (that is `RootView`); it survives as a preview-only reference sheet.
struct DesignTokensStorybook: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: FuelioSpacing.lg) {
                Text("Fuelio")
                    .font(.fuelio(.largeTitle, weight: .bold))
                    .foregroundStyle(FuelioColors.onSurface)

                colorSwatches
                typeScale
                spacingScale
            }
            .padding(FuelioSpacing.md)
        }
        .fuelioTheme()
    }

    private var colorSwatches: some View {
        VStack(alignment: .leading, spacing: FuelioSpacing.sm) {
            Text("Color").font(.fuelio(.headline))
            HStack(spacing: FuelioSpacing.sm) {
                swatch("Accent", FuelioColors.accent)
                swatch("Success", FuelioColors.success)
                swatch("Danger", FuelioColors.danger)
                swatch("Outline", FuelioColors.outline)
            }
        }
    }

    private func swatch(_ name: String, _ color: Color) -> some View {
        VStack(spacing: FuelioSpacing.xs) {
            RoundedRectangle(cornerRadius: FuelioRadius.medium)
                .fill(color)
                .frame(width: 48, height: 48)
            Text(name).font(.fuelio(.caption2))
        }
    }

    private var typeScale: some View {
        VStack(alignment: .leading, spacing: FuelioSpacing.xs) {
            Text("Typography").font(.fuelio(.headline))
            Text("Title Large").font(.fuelio(.title2, weight: .semibold))
            Text("Title Medium").font(.fuelio(.title3, weight: .medium))
            Text("Body Large").font(.fuelio(.body))
            Text("Label Small").font(.fuelio(.caption2, weight: .medium))
        }
    }

    private var spacingScale: some View {
        VStack(alignment: .leading, spacing: FuelioSpacing.xs) {
            Text("Spacing").font(.fuelio(.headline))
            HStack(spacing: FuelioSpacing.xs) {
                ForEach([FuelioSpacing.xxs, FuelioSpacing.xs, FuelioSpacing.sm, FuelioSpacing.md, FuelioSpacing.lg, FuelioSpacing.xl], id: \.self) { value in
                    Rectangle()
                        .fill(FuelioColors.accent)
                        .frame(width: value, height: 8)
                }
            }
        }
    }
}

#Preview("Light") {
    DesignTokensStorybook()
}

#Preview("Dark") {
    DesignTokensStorybook()
        .preferredColorScheme(.dark)
}
