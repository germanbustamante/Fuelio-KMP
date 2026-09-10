import SwiftUI
import CorePresentation
import KMPObservableViewModelSwiftUI

/// Same split as `GasStationsScreen`: the stateful half owns the Kotlin ViewModel, the stateless
/// `SettingsScreenBody` does the rendering so `#Preview` never touches Koin.
struct SettingsScreen: View {

    @StateViewModel private var viewModel = IosViewModelFactory.shared.settings()

    var body: some View {
        SettingsScreenBody(
            state: viewModel.state,
            onThemeModeSelected: { viewModel.onThemeModeSelected(themeMode: $0) },
            onDefaultFuelSelected: { viewModel.onDefaultFuelSelected(fuelType: $0) }
        )
    }
}

struct SettingsScreenBody: View {

    let state: SettingsUIState
    let onThemeModeSelected: (DomainThemeMode) -> Void
    let onDefaultFuelSelected: (DomainFuelType) -> Void

    var body: some View {
        // A grouped `List` is what Settings looks like on iOS. Android uses a scrolling column of
        // chip rows instead — same tokens, native idiom per platform, per ADR 0001.
        List {
            Section {
                ThemeModePicker(selected: state.themeMode, onSelect: onThemeModeSelected)
            } header: {
                Text("Appearance")
            }

            Section {
                DefaultFuelPicker(selected: state.defaultFuelType, onSelect: onDefaultFuelSelected)
            } header: {
                Text("Default fuel")
            } footer: {
                Text("The fuel the list is sorted and priced by when the app opens.")
            }
        }
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
        .accessibilityIdentifier(A11yID.settingsScreen)
    }
}

#Preview {
    NavigationStack {
        SettingsScreenBody(
            state: SettingsUIState(themeMode: .dark, defaultFuelType: .diesel, isLoading: false),
            onThemeModeSelected: { _ in },
            onDefaultFuelSelected: { _ in }
        )
    }
    .fuelioTheme()
}
