import SwiftUI
import CorePresentation

/// Province selector, presented as a sheet with detents — the iOS equivalent of Android's
/// `ModalBottomSheet`.
///
/// The province search is purely local UI filtering over an already-loaded list (Android does the
/// same in its bottom sheet); it is not the station search, which lives in the ViewModel.
struct ProvincePickerSheet: View {

    let provinces: [DomainProvinceBO]
    let selected: DomainProvinceBO?
    let onSelect: (DomainProvinceBO) -> Void
    let onDismiss: () -> Void

    @State private var query: String = ""

    private var filtered: [DomainProvinceBO] {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else { return provinces }
        return provinces.filter { $0.name.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        NavigationStack {
            List(filtered, id: \.id) { province in
                Button {
                    onSelect(province)
                    onDismiss()
                } label: {
                    HStack {
                        Text(province.name)
                            .foregroundStyle(FuelioColors.onSurface)
                        Spacer()
                        if province.id == selected?.id {
                            Image(systemName: "checkmark")
                                .foregroundStyle(FuelioColors.accent)
                        }
                    }
                    .contentShape(.rect)
                }
                .accessibilityIdentifier(A11yID.provinceRow(province.id))
            }
            .listStyle(.plain)
            .searchable(text: $query, prompt: Text("Search province…"))
            .navigationTitle("Select Province")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Close", systemImage: "xmark", action: onDismiss)
                        .accessibilityIdentifier(A11yID.provinceSheetClose)
                }
            }
        }
        .accessibilityIdentifier(A11yID.provinceSheet)
        .presentationDetents([.medium, .large])
    }
}

#Preview {
    ProvincePickerSheet(
        provinces: GasStationsFakesKt.fakeProvinces,
        selected: GasStationsFakesKt.fakeProvince,
        onSelect: { _ in },
        onDismiss: {}
    )
}
