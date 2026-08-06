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

    @NativeCoroutines
    suspend fun navigate(destination: Destination)

    @NativeCoroutines
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