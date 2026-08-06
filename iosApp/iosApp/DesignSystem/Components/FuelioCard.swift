import SwiftUI

/// Grouped surface used for list rows and detail sections.
///
/// Deliberately not a port of Material's `Card`: there is no tonal elevation (ADR 0003 keeps
/// elevation off the shared token list), depth is expressed with the surface colour plus a hairline
/// outline, which is how iOS groups content.
struct FuelioCard<Content: View>: View {

    private let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        content
            .background(FuelioColors.surface, in: .rect(cornerRadius: FuelioRadius.large))
            .overlay {
                RoundedRectangle(cornerRadius: FuelioRadius.large)
                    .strokeBorder(FuelioColors.outline.opacity(0.35), lineWidth: 1 / UIScreen.main.scale)
            }
    }
}

#Preview("Light") {
    FuelioCard {
        Text("Card content").padding(FuelioSpacing.md)
    }
    .padding(FuelioSpacing.md)
}

#Preview("Dark") {
    FuelioCard {
        Text("Card content").padding(FuelioSpacing.md)
    }
    .padding(FuelioSpacing.md)
    .preferredColorScheme(.dark)
}
