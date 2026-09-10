import SwiftUI
import CorePresentation
import KMPObservableViewModelSwiftUI

/// Owns the ViewModel's lifetime and forwards its state and actions to `GasStationsScreenBody`, which
/// does the actual rendering.
///
/// The split exists so `#Preview` can render `GasStationsScreenBody` directly with a fake
/// `GasStationsUIState` and no-op closures, never touching `IosViewModelFactory`/Koin or constructing
/// `viewModelScope`. Resolving the real ViewModel under Xcode's Previews JIT executor crashes (Kotlin/
/// Native's `Dispatchers.Main` initialization doesn't tolerate that non-standard launch path), so
/// previews must stay on the stateless half of the screen.
struct GasStationsScreen: View {

    /// `@StateViewModel` owns the Kotlin ViewModel's lifetime: it is created once per screen and
    /// cleared — cancelling `viewModelScope` — when the view goes away. It also observes the state
    /// annotated `@NativeCoroutinesState`, so there is no subscription to start or tear down here.
    @StateViewModel private var viewModel = IosViewModelFactory.shared.gasStations()

    var body: some View {
        GasStationsScreenBody(
            state: viewModel.state,
            onRefresh: { viewModel.onRefresh() },
            onProvinceSelected: { viewModel.onProvinceSelected(province: $0) },
            onFilterProvinceToggle: { viewModel.onFilterProvinceToggle(showFilterProvince: $0) },
            onOpenAppSettings: { viewModel.onOpenAppSettings() },
            onDismissPermissionSnackbar: { viewModel.onDismissPermissionSnackbar() },
            onDismissStaleDataError: { viewModel.onDismissStaleDataError() },
            onSearchQueryChanged: { viewModel.onSearchQueryChanged(query: $0) },
            onFuelFilterSelected: { viewModel.onFuelFilterSelected(filter: $0.kotlin) },
            onToggleFavorite: { viewModel.onToggleFavorite(stationId: $0) },
            onItemClick: { viewModel.onItemClick(stationId: $0) },
            onRetry: { viewModel.onRetry() },
            onDetectLocationTapped: { viewModel.onDetectLocationTapped() },
            onSettingsTapped: { viewModel.onSettingsTapped() }
        )
    }
}

/// Pure rendering over `state` and the actions it's handed — never reaches for the ViewModel or Koin
/// itself, which is what makes it safe to instantiate from `#Preview`.
struct GasStationsScreenBody: View {

    let state: GasStationsUIState
    let onRefresh: () -> Void
    let onProvinceSelected: (DomainProvinceBO) -> Void
    let onFilterProvinceToggle: (Bool) -> Void
    let onOpenAppSettings: () -> Void
    let onDismissPermissionSnackbar: () -> Void
    let onDismissStaleDataError: () -> Void
    let onSearchQueryChanged: (String) -> Void
    let onFuelFilterSelected: (FuelKind) -> Void
    let onToggleFavorite: (String) -> Void
    let onItemClick: (String) -> Void
    let onRetry: () -> Void
    let onDetectLocationTapped: () -> Void
    let onSettingsTapped: () -> Void

    var body: some View {
        content
            // Crossfade between skeleton / list / empty / error. Animating on a lightweight
            // discriminant rather than on `content` avoids comparing the whole station array — which
            // can be a couple of thousand Kotlin objects — on every render.
            .animation(.smooth(duration: 0.25), value: contentKind)
            .safeAreaInset(edge: .top, spacing: 0) {
                FuelFilterPicker(selection: fuelBinding)
            }
            .navigationTitle("Fuelio")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbar }
            .searchable(text: searchBinding, prompt: Text("Search gas station…"))
            .refreshable { onRefresh() }
            .sheet(isPresented: provinceSheetBinding) {
                ProvincePickerSheet(
                    provinces: state.provinces,
                    selected: state.selectedProvince,
                    onSelect: onProvinceSelected,
                    onDismiss: { onFilterProvinceToggle(false) }
                )
            }
            // Android shows a Snackbar with an action here. iOS has no Snackbar, and unlike the
            // stale-data notice this one needs a decision from the user, so an alert is the right
            // native equivalent.
            .alert("Location access needed", isPresented: permissionAlertBinding) {
                Button("Open Settings") { onOpenAppSettings() }
                    .accessibilityIdentifier(A11yID.permissionAlertSettings)
                Button("Cancel", role: .cancel) { onDismissPermissionSnackbar() }
            } message: {
                Text("Location access permanently denied. Enable it in Settings to see nearby stations.")
            }
            // A failed background refresh must never blank out data the user can already see, so it
            // surfaces as a transient banner instead of the blocking error state.
            .fuelioBanner(
                isPresented: state.staleDataError != nil,
                message: "Couldn't refresh. Showing saved data.",
                identifier: A11yID.staleDataBanner,
                onDismiss: onDismissStaleDataError
            )
    }

    // MARK: - Content

    private enum ContentKind: Hashable { case loading, list, empty, failure }

    private var contentKind: ContentKind {
        switch state.content {
        case .initial, .loading: .loading
        case .success: .list
        case .empty: .empty
        case .failure: .failure
        }
    }

    @ViewBuilder
    private var content: some View {
        switch state.content {
        case .initial, .loading:
            ScrollView {
                FuelioSkeletonList()
            }
            .accessibilityIdentifier(A11yID.loadingSkeleton)
            .background(FuelioColors.background)

        case .success(let stations):
            stationList(stations)

        case .empty:
            FuelioEmptyState(
                title: "No matches found",
                message: "No gas station matches that name or address. Try a different search term.",
                actionTitle: "Clear search",
                action: { onSearchQueryChanged("") }
            )
            // `ContentUnavailableView` builds its own accessibility container, which swallows a bare
            // `.accessibilityIdentifier`. Declaring the wrapper as a container makes it queryable.
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(A11yID.emptyState)

        case .failure(let message):
            FuelioErrorState(
                title: "Something went wrong",
                message: message,
                retryTitle: "Retry",
                retryIdentifier: A11yID.retryButton,
                onRetry: onRetry
            )
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(A11yID.errorState)
        }
    }

    private func stationList(_ stations: [GasStationItemVO]) -> some View {
        List {
            ForEach(stations, id: \.station.id) { item in
                // The exported `NSSet<NSString *>` bridges automatically to `Set<String>` here — no
                // manual conversion needed, since the header exposes it with its generic parameter.
                let favorites: Set<String> = state.favorites
                let isFavorite = favorites.contains(item.station.id)
                GasStationRow(
                    item: item,
                    isFavorite: isFavorite,
                    onToggleFavorite: { onToggleFavorite(item.station.id) }
                )
                .contentShape(.rect)
                // Routed through the ViewModel, not the router, so the selection analytics event
                // still fires.
                .onTapGesture { onItemClick(item.station.id) }
                .accessibilityAddTraits(.isButton)
                .accessibilityAction(named: isFavorite ? "Remove from favorites" : "Add to favorites") {
                    onToggleFavorite(item.station.id)
                }
                .swipeActions(edge: .leading, allowsFullSwipe: true) {
                    Button {
                        onToggleFavorite(item.station.id)
                    } label: {
                        Label(
                            isFavorite ? "Remove from favorites" : "Add to favorites",
                            systemImage: isFavorite ? "star.slash" : "star"
                        )
                    }
                    .tint(FuelioColors.accent)
                }
                .listRowSeparator(.hidden)
                .listRowBackground(Color.clear)
                .listRowInsets(EdgeInsets(
                    top: FuelioSpacing.xs,
                    leading: FuelioSpacing.md,
                    bottom: FuelioSpacing.xs,
                    trailing: FuelioSpacing.md
                ))
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .background(FuelioColors.background)
        .accessibilityIdentifier(A11yID.stationsList)
    }

    // MARK: - Toolbar

    @ToolbarContentBuilder
    private var toolbar: some ToolbarContent {
        ToolbarItem(placement: .principal) {
            ProvinceTitleButton(
                province: state.selectedProvince,
                onTap: { onFilterProvinceToggle(true) }
            )
        }

        ToolbarItem(placement: .topBarTrailing) {
            Button {
                onDetectLocationTapped()
            } label: {
                Label("Detect my location", systemImage: "location")
            }
            .accessibilityIdentifier(A11yID.detectLocationButton)
        }

        ToolbarItem(placement: .topBarTrailing) {
            Button {
                onSettingsTapped()
            } label: {
                Label("Settings", systemImage: "gearshape")
            }
            .accessibilityIdentifier(A11yID.settingsButton)
        }
    }

    // MARK: - Bindings

    /// The 300 ms debounce lives in `GasStationsViewModel`; adding another one here would only make
    /// the field feel laggy.
    private var searchBinding: Binding<String> {
        Binding(
            get: { state.searchQuery },
            set: { onSearchQueryChanged($0) }
        )
    }

    private var fuelBinding: Binding<FuelKind> {
        Binding(
            get: { state.fuelKind },
            set: { onFuelFilterSelected($0) }
        )
    }

    /// Dismissing without choosing (`Cancel`, or a system dismissal) still has to tell the ViewModel,
    /// or `showPermissionDeniedPermanentlySnackbar` would stay true and the alert would reappear.
    private var permissionAlertBinding: Binding<Bool> {
        Binding(
            get: { state.showPermissionDeniedPermanentlySnackbar },
            set: { isPresented in
                if !isPresented { onDismissPermissionSnackbar() }
            }
        )
    }

    /// One-way in practice: the ViewModel owns `showFilterProvince`, so the setter only forwards the
    /// dismissal (swipe-down included) back to it.
    private var provinceSheetBinding: Binding<Bool> {
        Binding(
            get: { state.showFilterProvince },
            set: { isPresented in
                if !isPresented { onFilterProvinceToggle(false) }
            }
        )
    }
}

#Preview {
    NavigationStack {
        GasStationsScreenBody(
            state: GasStationsFakesKt.fakeGasStationsUIState,
            onRefresh: {},
            onProvinceSelected: { _ in },
            onFilterProvinceToggle: { _ in },
            onOpenAppSettings: {},
            onDismissPermissionSnackbar: {},
            onDismissStaleDataError: {},
            onSearchQueryChanged: { _ in },
            onFuelFilterSelected: { _ in },
            onToggleFavorite: { _ in },
            onItemClick: { _ in },
            onRetry: {},
            onDetectLocationTapped: {},
            onSettingsTapped: {}
        )
    }
}

#Preview("Accessibility XXXL") {
    NavigationStack {
        GasStationsScreenBody(
            state: GasStationsFakesKt.fakeGasStationsUIState,
            onRefresh: {},
            onProvinceSelected: { _ in },
            onFilterProvinceToggle: { _ in },
            onOpenAppSettings: {},
            onDismissPermissionSnackbar: {},
            onDismissStaleDataError: {},
            onSearchQueryChanged: { _ in },
            onFuelFilterSelected: { _ in },
            onToggleFavorite: { _ in },
            onItemClick: { _ in },
            onRetry: {},
            onDetectLocationTapped: {},
            onSettingsTapped: {}
        )
    }
    .dynamicTypeSize(.accessibility3)
}

#Preview("Dark") {
    NavigationStack {
        GasStationsScreenBody(
            state: GasStationsFakesKt.fakeGasStationsUIState,
            onRefresh: {},
            onProvinceSelected: { _ in },
            onFilterProvinceToggle: { _ in },
            onOpenAppSettings: {},
            onDismissPermissionSnackbar: {},
            onDismissStaleDataError: {},
            onSearchQueryChanged: { _ in },
            onFuelFilterSelected: { _ in },
            onToggleFavorite: { _ in },
            onItemClick: { _ in },
            onRetry: {},
            onDetectLocationTapped: {},
            onSettingsTapped: {}
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("Error") {
    NavigationStack {
        GasStationsScreenBody(
            state: GasStationsFakesKt.fakeGasStationsUIStateError,
            onRefresh: {},
            onProvinceSelected: { _ in },
            onFilterProvinceToggle: { _ in },
            onOpenAppSettings: {},
            onDismissPermissionSnackbar: {},
            onDismissStaleDataError: {},
            onSearchQueryChanged: { _ in },
            onFuelFilterSelected: { _ in },
            onToggleFavorite: { _ in },
            onItemClick: { _ in },
            onRetry: {},
            onDetectLocationTapped: {},
            onSettingsTapped: {}
        )
    }
}
