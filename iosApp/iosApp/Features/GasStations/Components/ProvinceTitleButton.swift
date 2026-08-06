import SwiftUI
import CorePresentation

/// Navigation-bar title: the app name with the selected province underneath, tappable to change it.
///
/// Mirrors the Android top bar. `navigationSubtitle` only arrives in iOS 26, so on the 18.2
/// deployment target this is a `principal` toolbar item.
struct ProvinceTitleButton: View {

    let province: DomainProvinceBO?
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 0) {
                Text("Fuelio")
                    .font(.fuelio(.headline))
                    .foregroundStyle(FuelioColors.onSurface)
                if let province {
                    HStack(spacing: FuelioSpacing.xxs) {
                        Text(province.name)
                        Image(systemName: "chevron.down")
                    }
                    .font(.fuelio(.caption2))
                    .foregroundStyle(.secondary)
                }
            }
        }
        .buttonStyle(.plain)
        .disabled(province == nil)
        .accessibilityIdentifier(A11yID.provinceButton)
        .accessibilityLabel(
            province.map { String(localized: "Change province, currently \($0.name)") }
                ?? String(localized: "Fuelio")
        )
    }
}

#Preview {
    NavigationStack {
        Color.clear.toolbar {
            ToolbarItem(placement: .principal) {
                ProvinceTitleButton(province: GasStationsFakesKt.fakeProvince, onTap: {})
            }
        }
    }
}
