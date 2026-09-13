import SwiftUI
import CorePresentation

struct DefaultFuelPicker: View {

    let selected: DomainFuelType
    let onSelect: (DomainFuelType) -> Void
    var accessibilityID: String = A11yID.defaultFuelPicker

    var body: some View {
        Picker("Default fuel", selection: selectionBinding) {
            ForEach(DomainFuelType.allCases, id: \.self) { fuelType in
                Text(fuelType.kind.title)
                    .tag(fuelType)
                    .accessibilityIdentifier(A11yID.fuelOption(fuelType.kind.rawValue))
            }
        }
        .pickerStyle(.segmented)
        .accessibilityIdentifier(accessibilityID)
    }

    private var selectionBinding: Binding<DomainFuelType> {
        Binding(get: { selected }, set: onSelect)
    }
}
