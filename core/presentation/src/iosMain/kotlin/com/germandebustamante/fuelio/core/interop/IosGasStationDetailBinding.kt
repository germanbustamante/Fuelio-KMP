package com.germandebustamante.fuelio.core.interop

import com.germandebustamante.fuelio.feature.detail.state.GasStationDetailUIState
import com.germandebustamante.fuelio.feature.detail.state.GasStationDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/** Detail-screen counterpart of [IosGasStationsBinding]. */
class IosGasStationDetailBinding internal constructor(
    private val handle: IosViewModelHandle<GasStationDetailViewModel>,
    private val context: CoroutineContext = Dispatchers.Main.immediate,
) {
    val viewModel: GasStationDetailViewModel
        get() = handle.viewModel

    val currentState: GasStationDetailUIState
        get() = handle.viewModel.state.value

    fun observeState(onEach: (GasStationDetailUIState) -> Unit): FlowSubscription =
        handle.viewModel.state.subscribe(context, onEach)

    fun close() {
        handle.close()
    }
}
