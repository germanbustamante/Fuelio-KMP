import SwiftUI

/// Fuel selector.
///
/// Android uses a horizontal chip row; iOS uses a segmented `Picker`, which is the native control
/// for "pick exactly one of a small, fixed set" and comes with keyboard and VoiceOver behaviour for
/// free. The four options come from the Kotlin `FuelFilter` sealed interface, so a new fuel type
/// appears here automatically once `FuelKind` is extended.
struct FuelFilterPicker: View {

    @Binding var selection: FuelKind

    var body: some View {
        Picker("Fuel type", selection: $selection) {
            ForEach(FuelKind.allCases) { kind in
                Text(kind.title)
                    .tag(kind)
                    .accessibilityIdentifier(A11yID.fuelOption(kind.rawValue))
            }
        }
        .pickerStyle(.segmented)
        .accessibilityIdentifier(A11yID.fuelFilterPicker)
        .padding(.horizontal, FuelioSpacing.md)
        .padding(.vertical, FuelioSpacing.sm)
        .background(.bar)
    }
}

#Preview("Light") {
    @Previewable @State var selection = FuelKind.gasoline95
    return FuelFilterPicker(selection: $selection)
}

#Preview("Dark") {
    @Previewable @State var selection = FuelKind.diesel
    return FuelFilterPicker(selection: $selection)
        .preferredColorScheme(.dark)
}
