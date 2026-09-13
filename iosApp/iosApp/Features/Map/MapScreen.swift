import SwiftUI
import MapKit
import CorePresentation
import KMPObservableViewModelSwiftUI

struct MapScreen: View {

    @StateViewModel private var viewModel = IosViewModelFactory.shared.map()

    var body: some View {
        MapScreenBody(
            state: viewModel.state,
            onBackClick: { viewModel.onBackClick() },
            onMarkerSelected: { viewModel.onMarkerSelected(gasStationId: $0) }
        )
    }
}

/// Pure rendering over `state` — never reaches for the ViewModel or Koin itself, same split as
/// `GasStationDetailScreenBody`, which is what makes it safe to instantiate from `#Preview`.
struct MapScreenBody: View {

    let state: MapUIState
    var onBackClick: () -> Void = {}
    var onMarkerSelected: (String) -> Void = { _ in }

    var body: some View {
        content
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden()
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text("Stations map")
                }
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        onBackClick()
                    } label: {
                        Label("Back", systemImage: "chevron.backward")
                    }
                    .accessibilityIdentifier(A11yID.mapBackButton)
                }
            }
            .accessibilityIdentifier(A11yID.mapScreen)
    }

    @ViewBuilder
    private var content: some View {
        switch onEnum(of: state.contentState) {
        case .loading:
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)

        case .empty:
            FuelioEmptyState(
                title: "No stations to show",
                message: "Select a province from the list to see its gas stations on the map.",
                systemImage: "map"
            )

        case .error(let error):
            FuelioEmptyState(
                title: "Something went wrong",
                message: LocalizedStringKey(error.message),
                systemImage: "exclamationmark.triangle"
            )

        case .success(let success):
            ClusteredMapView(markers: success.markers, onMarkerSelected: onMarkerSelected)
                .ignoresSafeArea(edges: .bottom)
        }
    }
}

/// `MKMapView` wrapped for real pin clustering (`clusteringIdentifier`) — SwiftUI's native `Map` has
/// no clustering primitive of its own, only `MKMapViewDelegate`'s cluster annotation views do.
private struct ClusteredMapView: UIViewRepresentable {

    let markers: [MapMarkerVO]
    let onMarkerSelected: (String) -> Void

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.register(MKMarkerAnnotationView.self, forAnnotationViewWithReuseIdentifier: MKMapViewDefaultAnnotationViewReuseIdentifier)
        mapView.register(MKMarkerAnnotationView.self, forAnnotationViewWithReuseIdentifier: MKMapViewDefaultClusterAnnotationViewReuseIdentifier)
        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        let existingIds = Set((mapView.annotations.compactMap { $0 as? StationAnnotation }).map(\.gasStationId))
        let newIds = Set(markers.map(\.gasStationId))
        if existingIds != newIds {
            mapView.removeAnnotations(mapView.annotations)
            let annotations = markers.map(StationAnnotation.init)
            mapView.addAnnotations(annotations)
            mapView.showAnnotations(annotations, animated: false)
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onMarkerSelected: onMarkerSelected)
    }

    final class Coordinator: NSObject, MKMapViewDelegate {
        private let onMarkerSelected: (String) -> Void

        init(onMarkerSelected: @escaping (String) -> Void) {
            self.onMarkerSelected = onMarkerSelected
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            guard let station = annotation as? StationAnnotation else { return nil }
            let view = mapView.dequeueReusableAnnotationView(
                withIdentifier: MKMapViewDefaultAnnotationViewReuseIdentifier,
                for: station
            ) as? MKMarkerAnnotationView
            view?.clusteringIdentifier = "station"
            view?.canShowCallout = true
            return view
        }

        func mapView(_ mapView: MKMapView, didSelect annotation: any MKAnnotation) {
            if let station = annotation as? StationAnnotation {
                onMarkerSelected(station.gasStationId)
            } else if let cluster = annotation as? MKClusterAnnotation, let first = cluster.memberAnnotations.first as? StationAnnotation {
                // A tapped cluster has no single station to open — zoom in instead of picking one
                // arbitrarily; the user re-taps once the pins have spread out.
                mapView.setRegion(
                    MKCoordinateRegion(
                        center: cluster.coordinate,
                        latitudinalMeters: 2_000,
                        longitudinalMeters: 2_000
                    ),
                    animated: true
                )
                _ = first
            }
        }
    }
}

private final class StationAnnotation: NSObject, MKAnnotation {
    let gasStationId: String
    let coordinate: CLLocationCoordinate2D
    let title: String?
    let subtitle: String?

    init(marker: MapMarkerVO) {
        gasStationId = marker.gasStationId
        coordinate = CLLocationCoordinate2D(latitude: marker.latitude, longitude: marker.longitude)
        title = marker.displayName
        let price: Double? = marker.cheapestPrice?.doubleValue
        subtitle = price.map { NumberFormatterKt.formatAsEuros($0) }
    }
}

#Preview {
    NavigationStack {
        MapScreenBody(state: MapFakesKt.fakeMapUIState)
    }
}

#Preview("Empty") {
    NavigationStack {
        MapScreenBody(state: MapUIState(markers: [], isLoading: false, error: nil))
    }
}
