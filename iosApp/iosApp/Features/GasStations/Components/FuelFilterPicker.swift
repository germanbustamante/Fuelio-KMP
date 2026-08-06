import SwiftUI

/// Fuel selector.
///
/// Android uses a horizontal chip row; iOS uses a `Picker`, the native control for "pick exactly one
/// of a small, fixed set", which brings keyboard and VoiceOver behaviour for free. The four options
/// come from the Kotlin `FuelFilter` sealed interface, so a new fuel type appears here automatically
/// once `FuelKind` is extended.
struct FuelFilterPicker: View {

    @Binding var selection: FuelKind

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        content
            .accessibilityIdentifier(A11yID.fuelFilterPicker)
            .padding(.horizontal, FuelioSpacing.md)
            .padding(.vertical, FuelioSpacing.sm)
            .background(.bar)
    }

    /// Four segments cannot hold accessibility-size text without collapsing to ellipses, so the
    /// control degrades to a menu, which shows each label at full size.
    @ViewBuilder
    private var content: some View {
        if dynamicTypeSize.isAccessibilitySize {
            picker
                .pickerStyle(.menu)
                .frame(maxWidth: .infinity, alignment: .leading)
        } else {
            picker.pickerStyle(.segmented)
        }
    }

    private var picker: some View {
        Picker("Fuel type", selection: $selection) {
            ForEach(FuelKind.allCases) { kind in
                Text(kind.title)
                    .tag(kind)
                    .accessibilityIdentifier(A11yID.fuelOption(kind.rawValue))
            }
        }
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

#Preview("Accessibility XXXL") {
    @Previewable @State var selection = FuelKind.dieselPremium
    return FuelFilterPicker(selection: $selection)
        .dynamicTypeSize(.accessibility3)
}
