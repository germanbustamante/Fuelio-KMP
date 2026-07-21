package com.germandebustamante.fuelio.core.navigation.action

import com.germandebustamante.fuelio.core.navigation.destination.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface Navigator {
    val navigationActions: Flow<NavigationAction>

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