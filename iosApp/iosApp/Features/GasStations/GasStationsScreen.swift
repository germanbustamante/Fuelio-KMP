import SwiftUI
import CorePresentation

struct GasStationsScreen: View {

    @LazyStore(GasStationsStore.init) private var store

    var body: some View {
        Group {
            switch store.content {
            case .initial, .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .success(let stations):
                stationList(stations)
            case .empty:
                ContentUnavailableView("No matches found", systemImage: "magnifyingglass")
            case .failure(let message):
                ContentUnavailableView {
                    Label("Something went wrong", systemImage: "exclamationmark.triangle")
                } description: {
                    Text(message)
                } actions: {
                    Button("Retry") { store.retry() }
                }
            }
        }
        .navigationTitle("Fuelio")
        .task { store.activate() }
    }

    private func stationList(_ stations: [GasStationItemVO]) -> some View {
        List(stations, id: \.station.id) { item in
            Button {
                store.openStation(id: item.station.id)
            } label: {
                VStack(alignment: .leading, spacing: FuelioSpacing.xxs) {
                    Text(item.station.name)
                    Text(item.station.getFullDirection())
                        .font(.fuelio(.caption))
                        .foregroundStyle(.secondary)
                }
            }
            .buttonStyle(.plain)
        }
    }
}

#Preview {
    NavigationStack {
        GasStationsScreen()
    }
}
