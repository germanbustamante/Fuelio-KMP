import SwiftUI
import CorePresentation
import KMPObservableViewModelSwiftUI

/// Owns the ViewModel's lifetime and forwards its state and actions to `GasStationDetailScreenBody`,
/// which does the actual rendering — see `GasStationsScreen` for why the split exists (Previews cannot
/// resolve the real ViewModel via Koin without crashing under Xcode's Previews JIT executor).
struct GasStationDetailScreen: View {

    /// Created once per screen and cleared when the view goes away — see `GasStationsScreen`.
    @StateViewModel private var viewModel: GasStationDetailViewModel

    init(gasStationId: String) {
        _viewModel = StateViewModel(
            wrappedValue: IosViewModelFactory.shared.gasStationDetail(gasStationId: gasStationId)
        )
    }

    var body: some View {
        GasStationDetailScreenBody(
            state: viewModel.state,
            onBackClick: { viewModel.onBackClick() }
        )
    }
}

/// Pure rendering over `state` and the actions it's handed — never reaches for the ViewModel or Koin
/// itself, which is what makes it safe to instantiate from `#Preview`.
struct GasStationDetailScreenBody: View {

    let state: GasStationDetailUIState
    let onBackClick: () -> Void

    var body: some View {
        content
            .animation(.smooth(duration: 0.25), value: contentKind)
            .background(FuelioColors.background)
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden()
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    // Through the ViewModel, never by popping the stack directly, or the analytics
                    // attached to the action would never fire.
                    Button {
                        onBackClick()
                    } label: {
                        Label("Back", systemImage: "chevron.backward")
                    }
                    .accessibilityIdentifier(A11yID.detailBackButton)
                }
            }
    }

    private enum ContentKind: Hashable { case loading, loaded, notFound }

    private var contentKind: ContentKind {
        switch state.content {
        case .loading: .loading
        case .success: .loaded
        case .notFound: .notFound
        }
    }

    @ViewBuilder
    private var content: some View {
        switch state.content {
        case .loading:
            ScrollView { FuelioDetailSkeleton() }

        case .success(let station, let scheduleDays):
            loaded(station: station, scheduleDays: scheduleDays)

        case .notFound:
            // `getGasStationById` is a local-only read, so this means "not cached yet", not a failure
            // — hence an honest empty state rather than an error with a retry that could not help.
            FuelioEmptyState(
                title: "Gas station not available",
                message: "This station isn't cached locally yet. Go back to the list to load it first.",
                systemImage: "externaldrive.badge.questionmark"
            )
            .accessibilityIdentifier(A11yID.detailNotFound)
        }
    }

    private func loaded(station: DomainGasStationBO, scheduleDays: [ScheduleDayVO]) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: FuelioSpacing.lg) {
                header(station)
                StationMapSection(station: station)
                StationPricesSection(station: station)
                StationScheduleSection(days: scheduleDays)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(FuelioSpacing.md)
        }
    }

    private func header(_ station: DomainGasStationBO) -> some View {
        VStack(alignment: .leading, spacing: FuelioSpacing.xxs) {
            HStack(spacing: FuelioSpacing.sm) {
                BrandLogoView(brand: station.brand, size: FuelioSpacing.xl)
                Text(station.displayName)
                    .font(.fuelio(.title2, weight: .semibold))
                    .accessibilityIdentifier(A11yID.detailStationName)
            }
            Text(station.getFullDirection())
                .font(.fuelio(.callout))
                .foregroundStyle(.secondary)
        }
        .accessibilityElement(children: .combine)
    }
}

#Preview("Light") {
    NavigationStack {
        GasStationDetailScreenBody(state: GasStationDetailFakesKt.fakeGasStationDetailUIState, onBackClick: {})
    }
}

#Preview("Dark") {
    NavigationStack {
        GasStationDetailScreenBody(state: GasStationDetailFakesKt.fakeGasStationDetailUIState, onBackClick: {})
    }
    .preferredColorScheme(.dark)
}

#Preview("Accessibility XXXL") {
    NavigationStack {
        GasStationDetailScreenBody(state: GasStationDetailFakesKt.fakeGasStationDetailUIState, onBackClick: {})
    }
    .dynamicTypeSize(.accessibility3)
}

#Preview("Not Found") {
    NavigationStack {
        GasStationDetailScreenBody(state: GasStationDetailFakesKt.fakeGasStationDetailUIStateNotFound, onBackClick: {})
    }
}

#Preview("Loading") {
    NavigationStack {
        GasStationDetailScreenBody(state: GasStationDetailFakesKt.fakeGasStationDetailUIStateLoading, onBackClick: {})
    }
}
