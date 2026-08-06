import SwiftUI
import MapKit
import CorePresentation

/// Real MapKit map plus a directions hand-off.
///
/// Android shows a placeholder box here (`detail_map_placeholder`); iOS gets the real thing because
/// MapKit is free on the platform. Directions go through `MKMapItem.openInMaps`, which respects the
/// user's default navigation app choice, rather than hard-coding a Google Maps URL.
struct StationMapSection: View {

    let station: DomainGasStationBO

    private var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: station.latitude, longitude: station.longitude)
    }

    var body: some View {
        VStack(spacing: FuelioSpacing.sm) {
            Map(initialPosition: .region(
                MKCoordinateRegion(center: coordinate, latitudinalMeters: 600, longitudinalMeters: 600)
            )) {
                Marker(station.displayName, systemImage: BrandLogo.fallbackSymbol, coordinate: coordinate)
                    .tint(FuelioColors.accent)
            }
            .mapControlVisibility(.hidden)
            .frame(height: 180)
            .clipShape(.rect(cornerRadius: FuelioRadius.large))
            .allowsHitTesting(false)
            .accessibilityLabel(String(localized: "Map showing \(station.displayName)"))

            Button {
                openDirections()
            } label: {
                Label("Get directions", systemImage: "arrow.triangle.turn.up.right.circle")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .tint(FuelioColors.accent)
            .accessibilityIdentifier(A11yID.detailDirectionsButton)
        }
    }

    private func openDirections() {
        let mapItem = MKMapItem(placemark: MKPlacemark(coordinate: coordinate))
        mapItem.name = station.displayName
        mapItem.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving])
    }
}

#Preview {
    StationMapSection(station: FakeGasStationsKt.fakeGasStations[0])
        .padding(FuelioSpacing.md)
}
