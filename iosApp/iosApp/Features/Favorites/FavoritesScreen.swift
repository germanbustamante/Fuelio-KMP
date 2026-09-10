import SwiftUI
import CorePresentation
import KMPObservableViewModelSwiftUI

/// Same split as every other screen: the stateful half owns the Kotlin ViewModel, the stateless
/// `FavoritesScreenBody` renders, so `#Preview` never resolves anything from Koin.
struct FavoritesScreen: View {

    @StateViewModel private var viewModel = IosViewModelFactory.shared.favorites()

    var body: some View {
        FavoritesScreenBody(
            state: viewModel.state,
            onItemClick: { viewModel.onItemClick(stationId: $0) },
            onToggleFavorite: { viewModel.onToggleFavorite(stationId: $0) },
            onBackTapped: { viewModel.onBackTapped() }
        )
    }
}

struct FavoritesScreenBody: View {

    let state: FavoritesUIState
    let onItemClick: (String) -> Void
    let onToggleFavorite: (String) -> Void
    let onBackTapped: () -> Void

    var body: some View {
        content
            // A container, not a merged element: without `children: .contain` the screen identifier
            // lands on whatever single element the content collapses into — the empty state merges
            // itself, so `favorites_screen` would overwrite `favorites_empty` on the very same node.
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(A11yID.favoritesScreen)
            .animation(.smooth(duration: 0.25), value: contentKind)
            .background(FuelioColors.background)
            .navigationTitle("Favorites")
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
                    .accessibilityIdentifier(A11yID.favoritesBackButton)
                }
            }
    }

    // MARK: - Content

    private enum ContentKind: Hashable { case loading, list, empty }

    private var contentKind: ContentKind {
        switch state.content {
        case .loading: .loading
        case .success: .list
        case .empty: .empty
        }
    }

    @ViewBuilder
    private var content: some View {
        switch state.content {
        case .loading:
            ScrollView {
                FuelioSkeletonList()
            }
            .accessibilityIdentifier(A11yID.loadingSkeleton)
            .background(FuelioColors.background)

        case .empty:
            FuelioEmptyState(
                title: "No favorites yet",
                message: "Tap the star on any gas station to keep it here and compare prices at a glance.",
                systemImage: "star"
            )
            // `ContentUnavailableView` builds its own accessibility container; merging it gives the
            // identifier a single element to land on. An empty state has nothing to navigate into,
            // so collapsing it costs VoiceOver nothing.
            .accessibilityElement(children: .combine)
            .accessibilityIdentifier(A11yID.favoritesEmpty)

        case .success(let stations, let unresolvedCount):
            favoritesList(stations, unresolvedCount: unresolvedCount)
        }
    }

    private func favoritesList(_ stations: [GasStationItemVO], unresolvedCount: Int) -> some View {
        List {
            if unresolvedCount > 0 {
                unresolvedBanner(count: unresolvedCount)
            }

            ForEach(stations, id: \.station.id) { item in
                // Everything on this screen is a favourite by definition, so the star is always on
                // and tapping it removes the station from the list.
                GasStationRow(
                    item: item,
                    isFavorite: true,
                    onToggleFavorite: { onToggleFavorite(item.station.id) }
                )
                .contentShape(.rect)
                .onTapGesture { onItemClick(item.station.id) }
                .accessibilityAddTraits(.isButton)
                .accessibilityAction(named: "Remove from favorites") {
                    onToggleFavorite(item.station.id)
                }
                .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                    Button(role: .destructive) {
                        onToggleFavorite(item.station.id)
                    } label: {
                        Label("Remove from favorites", systemImage: "star.slash")
                    }
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
    }

    /// Favourites whose station isn't cached right now can't be rendered — say so rather than
    /// looking like the app lost them.
    private func unresolvedBanner(count: Int) -> some View {
        Text(
            String.localizedStringWithFormat(
                NSLocalizedString(
                    "%lld favorites aren't loaded. Open their provinces to see them.",
                    comment: "Banner shown when some favourites' stations are not cached"
                ),
                count
            )
        )
        .font(.fuelio(.footnote))
        .foregroundStyle(.secondary)
        .frame(maxWidth: .infinity, alignment: .leading)
        .listRowSeparator(.hidden)
        .listRowBackground(Color.clear)
        .listRowInsets(EdgeInsets(
            top: FuelioSpacing.sm,
            leading: FuelioSpacing.md,
            bottom: FuelioSpacing.sm,
            trailing: FuelioSpacing.md
        ))
        .accessibilityIdentifier(A11yID.favoritesUnresolvedBanner)
    }
}

#Preview("Empty") {
    NavigationStack {
        FavoritesScreenBody(
            state: FavoritesUIState(stations: [], unresolvedCount: 0, isLoading: false),
            onItemClick: { _ in },
            onToggleFavorite: { _ in },
            onBackTapped: {}
        )
    }
    .fuelioTheme()
}
