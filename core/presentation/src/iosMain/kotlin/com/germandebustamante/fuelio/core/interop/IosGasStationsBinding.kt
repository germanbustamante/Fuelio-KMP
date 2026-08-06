package com.germandebustamante.fuelio.core.interop

import com.germandebustamante.fuelio.feature.list.state.GasStationsUIState
import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Everything the SwiftUI gas stations screen needs, with no erased generics in the signature.
 *
 * Swift talks to [viewModel] for actions (they are plain, exported instance methods) and to
 * [observeState] for state — never to `state`, which the Objective-C exporter degrades to an
 * unparameterized `StateFlow`.
 */
class IosGasStationsBinding internal constructor(
    private val handle: IosViewModelHandle<GasStationsViewModel>,
    private val context: CoroutineContext = Dispatchers.Main.immediate,
) {
    val viewModel: GasStationsViewModel
        get() = handle.viewModel

    /** The state as of right now, so Swift can render a first frame without waiting for an emission. */
    val currentState: GasStationsUIState
        get() = handle.viewModel.state.value

    fun observeState(onEach: (GasStationsUIState) -> Unit): FlowSubscription =
        handle.viewModel.state.subscribe(context, onEach)

    /** Ends `viewModelScope`. Must be called when the screen goes away. */
    fun close() {
        handle.close()
    }
}
