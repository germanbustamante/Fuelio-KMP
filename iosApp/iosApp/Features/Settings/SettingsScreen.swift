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
            onDefaultFuelSelected: { viewModel.onDefaultFuelSelected(fuelType: $0) },
            onBackTapped: { viewModel.onBackTapped() }
        )
    }
}

struct SettingsScreenBody: View {

    let state: SettingsUIState
    let onThemeModeSelected: (DomainThemeMode) -> Void
    let onDefaultFuelSelected: (DomainFuelType) -> Void
    let onBackTapped: () -> Void

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
        .navigationBarBackButtonHidden()
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                // Through the ViewModel, never by popping the stack directly, or the analytics
                // attached to the action would never fire.
                Button {
                    onBackTapped()
                } label: {
                    Label("Back", systemImage: "chevron.backward")
                }
                .accessibilityIdentifier(A11yID.settingsBackButton)
            }
        }
        .accessibilityIdentifier(A11yID.settingsScreen)
    }
}

#Preview {
    NavigationStack {
        SettingsScreenBody(
            state: SettingsUIState(themeMode: .dark, defaultFuelType: .diesel, isLoading: false),
            onThemeModeSelected: { _ in },
            onDefaultFuelSelected: { _ in },
            onBackTapped: {}
        )
    }
    .fuelioTheme()
}
