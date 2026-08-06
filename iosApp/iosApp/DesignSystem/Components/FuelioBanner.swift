import SwiftUI

/// Transient notice anchored to the bottom of a screen.
///
/// iOS has no Snackbar, and an `alert` would be far too heavy for "the background refresh failed but
/// your data is still here". This is the closest native equivalent: a non-blocking banner over a
/// `.regularMaterial` background that auto-dismisses.
struct FuelioBanner: View {

    let message: LocalizedStringKey
    var systemImage: String = "wifi.exclamationmark"

    var body: some View {
        Label(message, systemImage: systemImage)
            .font(.fuelio(.footnote))
            .foregroundStyle(FuelioColors.onSurface)
            .padding(.horizontal, FuelioSpacing.md)
            .padding(.vertical, FuelioSpacing.sm)
            .background(.regularMaterial, in: .capsule)
            .overlay {
                Capsule().strokeBorder(FuelioColors.outline.opacity(0.35), lineWidth: 1 / UIScreen.main.scale)
            }
            .shadow(radius: FuelioRadius.extraSmall, y: 1)
            .padding(.bottom, FuelioSpacing.md)
            .transition(.move(edge: .bottom).combined(with: .opacity))
    }
}

extension View {

    /// Presents a `FuelioBanner` over the bottom of the receiver while `isPresented` is true, and
    /// calls `onDismiss` after `duration`. Mirrors the Android Snackbar behaviour for stale data.
    func fuelioBanner(
        isPresented: Bool,
        message: LocalizedStringKey,
        systemImage: String = "wifi.exclamationmark",
        identifier: String? = nil,
        duration: Duration = .seconds(4),
        onDismiss: @escaping () -> Void
    ) -> some View {
        overlay(alignment: .bottom) {
            if isPresented {
                FuelioBanner(message: message, systemImage: systemImage)
                    .accessibilityIdentifier(identifier ?? "")
                    .task {
                        try? await Task.sleep(for: duration)
                        onDismiss()
                    }
            }
        }
        .animation(.snappy, value: isPresented)
    }
}

#Preview("Light") {
    Color.clear
        .fuelioBanner(isPresented: true, message: "Couldn't refresh. Showing saved data.") {}
}

#Preview("Dark") {
    Color.clear
        .fuelioBanner(isPresented: true, message: "Couldn't refresh. Showing saved data.") {}
        .preferredColorScheme(.dark)
}
