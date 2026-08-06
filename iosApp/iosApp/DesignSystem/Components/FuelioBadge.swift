import SwiftUI

/// Small status pill — open/closed, "cheapest", and anything else that needs a one-word marker.
struct FuelioBadge: View {

    enum Tone {
        case success
        case danger
        case accent
        case neutral

        var foreground: Color {
            switch self {
            case .success: FuelioColors.success
            case .danger: FuelioColors.danger
            case .accent: FuelioColors.accent
            case .neutral: Color.secondary
            }
        }
    }

    let title: LocalizedStringKey
    let systemImage: String?
    let tone: Tone

    init(_ title: LocalizedStringKey, systemImage: String? = nil, tone: Tone) {
        self.title = title
        self.systemImage = systemImage
        self.tone = tone
    }

    var body: some View {
        HStack(spacing: FuelioSpacing.xxs) {
            if let systemImage {
                Image(systemName: systemImage)
            }
            Text(title)
        }
        .font(.fuelio(.caption2, weight: .medium))
        .foregroundStyle(tone.foreground)
        .padding(.horizontal, FuelioSpacing.sm)
        .padding(.vertical, FuelioSpacing.xxs)
        .background(tone.foreground.opacity(0.12), in: .capsule)
    }
}

#Preview("Light") {
    HStack {
        FuelioBadge("Open", systemImage: "clock", tone: .success)
        FuelioBadge("Closed", systemImage: "clock", tone: .danger)
        FuelioBadge("Cheapest", systemImage: "tag.fill", tone: .accent)
    }
    .padding(FuelioSpacing.md)
}

#Preview("Dark") {
    HStack {
        FuelioBadge("Open", systemImage: "clock", tone: .success)
        FuelioBadge("Cheapest", systemImage: "tag.fill", tone: .accent)
    }
    .padding(FuelioSpacing.md)
    .preferredColorScheme(.dark)
}
