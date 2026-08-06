import CorePresentation

/// Detail-screen counterpart of `GasStationsBackend` — see that file for why the seam exists.
@MainActor
protocol GasStationDetailBackend: AnyObject {
    var currentState: GasStationDetailUIState { get }
    func observeState(_ onEach: @escaping (GasStationDetailUIState) -> Void) -> FlowSubscription
    func close()

    func goBack()
}

@MainActor
final class KotlinGasStationDetailBackend: GasStationDetailBackend {

    private let binding: IosGasStationDetailBinding

    init(gasStationId: String) {
        self.binding = IosBindingFactory.shared.createGasStationDetailBinding(gasStationId: gasStationId)
    }

    init(binding: IosGasStationDetailBinding) {
        self.binding = binding
    }

    var currentState: GasStationDetailUIState { binding.currentState }

    func observeState(_ onEach: @escaping (GasStationDetailUIState) -> Void) -> FlowSubscription {
        binding.observeState(onEach: onEach)
    }

    func close() { binding.close() }

    func goBack() { binding.viewModel.onBackClick() }
}
