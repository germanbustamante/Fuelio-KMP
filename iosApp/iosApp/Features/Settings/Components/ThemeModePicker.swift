import SwiftUI
import CorePresentation

/// SKIE exports the Kotlin `DomainThemeMode` enum as a real Swift enum, so `allCases` and an exhaustive
/// `switch` both work — adding a mode in Kotlin is a compile error here.
struct ThemeModePicker: View {

    let selected: DomainThemeMode
    let onSelect: (DomainThemeMode) -> Void

    var body: some View {
        Picker("Appearance", selection: selectionBinding) {
            ForEach(DomainThemeMode.allCases, id: \.self) { mode in
                Text(mode.title)
                    .tag(mode)
                    .accessibilityIdentifier(A11yID.themeOption(mode.rawIdentifier))
            }
        }
        .pickerStyle(.segmented)
        .accessibilityIdentifier(A11yID.themePicker)
    }

    private var selectionBinding: Binding<DomainThemeMode> {
        Binding(get: { selected }, set: onSelect)
    }
}

extension DomainThemeMode {

    var title: LocalizedStringKey {
        switch self {
        case .system: return "System"
        case .light: return "Light"
        case .dark: return "Dark"
        }
    }

    /// Derived from the Kotlin enum's own name so this can't drift from Android's identifier.
    var rawIdentifier: String { name.lowercased() }
}
