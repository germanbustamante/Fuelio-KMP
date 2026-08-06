import SwiftUI
import CorePresentation

struct GasStationDetailScreen: View {

    @LazyStore private var store: GasStationDetailStore

    init(gasStationId: String) {
        _store = LazyStore { GasStationDetailStore(gasStationId: gasStationId) }
    }

    var body: some View {
        Group {
            switch store.content {
            case .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .success(let station, _):
                VStack(alignment: .leading, spacing: FuelioSpacing.sm) {
                    Text(station.name).font(.fuelio(.title2, weight: .semibold))
                    Text(station.getFullDirection()).foregroundStyle(.secondary)
                    Spacer()
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(FuelioSpacing.md)
            case .notFound:
                ContentUnavailableView("Gas station not available", systemImage: "exclamationmark.triangle")
            }
        }
        .navigationBarBackButtonHidden()
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    store.goBack()
                } label: {
                    Label("Back", systemImage: "chevron.backward")
                }
            }
        }
        .task { store.activate() }
    }
}

#Preview {
    NavigationStack {
        GasStationDetailScreen(gasStationId: "7153")
    }
}
