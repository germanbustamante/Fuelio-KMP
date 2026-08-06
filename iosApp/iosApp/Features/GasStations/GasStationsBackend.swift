import CorePresentation

/// Seam between `GasStationsStore` and the Kotlin ViewModel.
///
/// The exported `GasStationsViewModel` is `objc_subclassing_restricted`, so it cannot be spied on
/// from Swift. This protocol is what lets the store's delegation and lifetime be unit-tested; it
/// mirrors the ViewModel one-to-one and adds no behaviour of its own.
@MainActor
protocol GasStationsBackend: AnyObject {
    var currentState: GasStationsUIState { get }
    func observeState(_ onEach: @escaping (GasStationsUIState) -> Void) -> any StateSubscription
    func close()

    func detectLocation()
    func setProvinceFilterVisible(_ visible: Bool)
    func selectProvince(_ province: DomainProvinceBO)
    func selectFuelFilter(_ filter: FuelFilter)
    func search(_ query: String)
    func dismissError()
    func dismissStaleDataError()
    func dismissPermissionAlert()
    func openAppSettings()
    func refresh()
    func retry()
    func openStation(id: String)
    func toggleFavorite(id: String)
}

/// Production implementation: a thin pass-through to `IosGasStationsBinding`.
@MainActor
final class KotlinGasStationsBackend: GasStationsBackend {

    private let binding: IosGasStationsBinding

    init(binding: IosGasStationsBinding = IosBindingFactory.shared.createGasStationsBinding()) {
        self.binding = binding
    }

    var currentState: GasStationsUIState { binding.currentState }

    func observeState(_ onEach: @escaping (GasStationsUIState) -> Void) -> any StateSubscription {
        binding.observeState(onEach: onEach)
    }

    func close() { binding.close() }

    func detectLocation() { binding.viewModel.onDetectLocationTapped() }
    func setProvinceFilterVisible(_ visible: Bool) { binding.viewModel.onFilterProvinceToggle(showFilterProvince: visible) }
    func selectProvince(_ province: DomainProvinceBO) { binding.viewModel.onProvinceSelected(province: province) }
    func selectFuelFilter(_ filter: FuelFilter) { binding.viewModel.onFuelFilterSelected(filter: filter) }
    func search(_ query: String) { binding.viewModel.onSearchQueryChanged(query: query) }
    func dismissError() { binding.viewModel.onDismissError() }
    func dismissStaleDataError() { binding.viewModel.onDismissStaleDataError() }
    func dismissPermissionAlert() { binding.viewModel.onDismissPermissionSnackbar() }
    func openAppSettings() { binding.viewModel.onOpenAppSettings() }
    func refresh() { binding.viewModel.onRefresh() }
    func retry() { binding.viewModel.onRetry() }
    func openStation(id: String) { binding.viewModel.onItemClick(stationId: id) }
    func toggleFavorite(id: String) { binding.viewModel.onToggleFavorite(stationId: id) }
}
