import SwiftUI
import CorePresentation

struct GasStationsScreen: View {

    @LazyStore(GasStationsStore.init) private var store

    var body: some View {
        content
            .safeAreaInset(edge: .top, spacing: 0) {
                FuelFilterPicker(selection: fuelBinding)
            }
            .navigationTitle("Fuelio")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbar }
            .searchable(text: searchBinding, prompt: Text("Search gas station…"))
            .refreshable { store.refresh() }
            .sheet(isPresented: provinceSheetBinding) {
                ProvincePickerSheet(
                    provinces: store.provinces,
                    selected: store.selectedProvince,
                    onSelect: { store.selectProvince($0) },
                    onDismiss: { store.dismissProvincePicker() }
                )
            }
            // Android shows a Snackbar with an action here. iOS has no Snackbar, and unlike the
            // stale-data notice this one needs a decision from the user, so an alert is the right
            // native equivalent.
            .alert("Location access needed", isPresented: permissionAlertBinding) {
                Button("Open Settings") { store.openAppSettings() }
                    .accessibilityIdentifier(A11yID.permissionAlertSettings)
                Button("Cancel", role: .cancel) { store.dismissPermissionAlert() }
            } message: {
                Text("Location access permanently denied. Enable it in Settings to see nearby stations.")
            }
            // A failed background refresh must never blank out data the user can already see, so it
            // surfaces as a transient banner instead of the blocking error state.
            .fuelioBanner(
                isPresented: store.hasStaleDataError,
                message: "Couldn't refresh. Showing saved data.",
                identifier: A11yID.staleDataBanner,
                onDismiss: { store.dismissStaleDataError() }
            )
            .task { store.activate() }
    }

    // MARK: - Content

    @ViewBuilder
    private var content: some View {
        switch store.content {
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
                action: { store.search("") }
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
                onRetry: { store.retry() }
            )
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(A11yID.errorState)
        }
    }

    private func stationList(_ stations: [GasStationItemVO]) -> some View {
        List {
            ForEach(stations, id: \.station.id) { item in
                let isFavorite = store.isFavorite(item.station.id)
                GasStationRow(
                    item: item,
                    isFavorite: isFavorite,
                    onToggleFavorite: { store.toggleFavorite(id: item.station.id) }
                )
                .contentShape(.rect)
                .onTapGesture { store.openStation(id: item.station.id) }
                .accessibilityAddTraits(.isButton)
                .accessibilityAction(named: isFavorite ? "Remove from favorites" : "Add to favorites") {
                    store.toggleFavorite(id: item.station.id)
                }
                .swipeActions(edge: .leading, allowsFullSwipe: true) {
                    Button {
                        store.toggleFavorite(id: item.station.id)
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
                province: store.selectedProvince,
                onTap: { store.presentProvincePicker() }
            )
        }

        ToolbarItem(placement: .topBarTrailing) {
            Button {
                store.detectLocation()
            } label: {
                Label("Detect my location", systemImage: "location")
            }
            .accessibilityIdentifier(A11yID.detectLocationButton)
        }
    }

    // MARK: - Bindings

    /// The 300 ms debounce lives in `GasStationsViewModel`; adding another one here would only make
    /// the field feel laggy.
    private var searchBinding: Binding<String> {
        Binding(
            get: { store.searchQuery },
            set: { store.search($0) }
        )
    }

    private var fuelBinding: Binding<FuelKind> {
        Binding(
            get: { store.selectedFuel },
            set: { store.selectFuel($0) }
        )
    }

    /// Dismissing without choosing (`Cancel`, or a system dismissal) still has to tell the ViewModel,
    /// or `showPermissionDeniedPermanentlySnackbar` would stay true and the alert would reappear.
    private var permissionAlertBinding: Binding<Bool> {
        Binding(
            get: { store.isPermissionAlertPresented },
            set: { isPresented in
                if !isPresented { store.dismissPermissionAlert() }
            }
        )
    }

    /// One-way in practice: the ViewModel owns `showFilterProvince`, so the setter only forwards the
    /// dismissal (swipe-down included) back to it.
    private var provinceSheetBinding: Binding<Bool> {
        Binding(
            get: { store.isProvincePickerPresented },
            set: { isPresented in
                if !isPresented { store.dismissProvincePicker() }
            }
        )
    }
}

#Preview {
    NavigationStack {
        GasStationsScreen()
    }
}
