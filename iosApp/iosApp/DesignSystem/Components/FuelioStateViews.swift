import SwiftUI

/// Empty and error placeholders.
///
/// Both wrap `ContentUnavailableView` rather than reimplementing a Material-style empty state: it is
/// the system component for exactly this, and it already handles Dynamic Type, dark mode and
/// VoiceOver grouping. The wrappers only pin the Fuelio iconography and tint.
struct FuelioEmptyState: View {

    let title: LocalizedStringKey
    let message: LocalizedStringKey
    var systemImage: String = "magnifyingglass"
    var actionTitle: LocalizedStringKey?
    var action: (() -> Void)?

    var body: some View {
        ContentUnavailableView {
            Label(title, systemImage: systemImage)
        } description: {
            Text(message)
        } actions: {
            if let actionTitle, let action {
                Button(actionTitle, action: action)
                    .buttonStyle(.borderedProminent)
                    .tint(FuelioColors.accent)
            }
        }
    }
}

struct FuelioErrorState: View {

    let title: LocalizedStringKey
    let message: String
    let retryTitle: LocalizedStringKey
    var retryIdentifier: String?
    let onRetry: () -> Void

    var body: some View {
        ContentUnavailableView {
            Label(title, systemImage: "exclamationmark.triangle")
                .foregroundStyle(FuelioColors.danger)
        } description: {
            Text(message)
        } actions: {
            Button(retryTitle, action: onRetry)
                .buttonStyle(.borderedProminent)
                .tint(FuelioColors.accent)
                .accessibilityIdentifier(retryIdentifier ?? "")
        }
    }
}

#Preview("Empty") {
    FuelioEmptyState(
        title: "No matches found",
        message: "No gas station matches that name or address. Try a different search term.",
        actionTitle: "Clear search",
        action: {}
    )
}

#Preview("Error") {
    FuelioErrorState(
        title: "Something went wrong",
        message: "Server error: 403",
        retryTitle: "Retry",
        onRetry: {}
    )
}

#Preview("Error · Dark") {
    FuelioErrorState(
        title: "Something went wrong",
        message: "Server error: 403",
        retryTitle: "Retry",
        onRetry: {}
    )
    .preferredColorScheme(.dark)
}
