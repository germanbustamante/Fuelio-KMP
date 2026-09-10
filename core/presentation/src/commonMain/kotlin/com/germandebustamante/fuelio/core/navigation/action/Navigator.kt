package com.germandebustamante.fuelio.core.navigation.action

import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface Navigator {
    /**
     * Channel-backed and therefore **single-consumer**: a second subscriber steals events and
     * navigation silently stops working. Exactly one observer, alive for the whole app, at the root.
     */
    @NativeCoroutines
    val navigationActions: Flow<NavigationAction>

    // Deliberately *not* annotated. `@NativeCoroutines` replaces the exported `async` function with a
    // closure that must be invoked through `asyncFunction(for:)`; calling it as `try await` still
    // compiles (with only a warning) but silently does nothing. Swift never navigates directly —
    // screens call a ViewModel action — so the plain export is both safer and closer to the
    // invariant.
    suspend fun navigate(destination: Destination)

    suspend fun navigateUp()
}

class DefaultNavigator : Navigator {

    private val _navigationActions = Channel<NavigationAction>()
    override val navigationActions: Flow<NavigationAction> = _navigationActions.receiveAsFlow()

    override suspend fun navigate(destination: Destination) {
        _navigationActions.send(NavigationAction.Navigate(destination = destination))
    }

    override suspend fun navigateUp() {
        _navigationActions.send(NavigationAction.Back)
    }
}
