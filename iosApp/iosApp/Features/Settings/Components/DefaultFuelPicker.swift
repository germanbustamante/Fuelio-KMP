import SwiftUI
import CorePresentation

struct DefaultFuelPicker: View {

    let selected: DomainFuelType
    let onSelect: (DomainFuelType) -> Void

    var body: some View {
        Picker("Default fuel", selection: selectionBinding) {
            ForEach(DomainFuelType.allCases, id: \.self) { fuelType in
                Text(fuelType.kind.title)
                    .tag(fuelType)
                    .accessibilityIdentifier(A11yID.fuelOption(fuelType.kind.rawValue))
            }
        }
        .pickerStyle(.segmented)
        .accessibilityIdentifier(A11yID.defaultFuelPicker)
    }

    private var selectionBinding: Binding<DomainFuelType> {
        Binding(get: { selected }, set: onSelect)
    }
}
