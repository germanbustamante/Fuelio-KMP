import SwiftUI
import CorePresentation

struct GasStationsScreen: View {

    @LazyStore(GasStationsStore.init) private var store

    var body: some View {
        content
            .navigationTitle("Fuelio")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbar }
            .searchable(text: searchBinding, prompt: Text("Search gas station…"))
            .refreshable { store.refresh() }
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
            .accessibilityIdentifier(A11yID.emptyState)

        case .failure(let message):
            FuelioErrorState(
                title: "Something went wrong",
                message: message,
                retryTitle: "Retry",
                onRetry: { store.retry() }
            )
            .accessibilityIdentifier(A11yID.errorState)
        }
    }

    private func stationList(_ stations: [GasStationItemVO]) -> some View {
        List {
            ForEach(stations, id: \.station.id) { item in
                GasStationRow(
                    item: item,
                    isFavorite: store.isFavorite(item.station.id),
                    onToggleFavorite: { store.toggleFavorite(id: item.station.id) }
                )
                .contentShape(.rect)
                .onTapGesture { store.openStation(id: item.station.id) }
                .accessibilityAddTraits(.isButton)
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
}

#Preview {
    NavigationStack {
        GasStationsScreen()
    }
}
