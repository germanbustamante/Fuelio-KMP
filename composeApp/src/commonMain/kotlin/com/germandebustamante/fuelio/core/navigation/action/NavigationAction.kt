package com.germandebustamante.fuelio.core.navigation.action

import com.germandebustamante.fuelio.core.navigation.destination.Destination

sealed interface NavigationAction {
    data class Navigate(val destination: Destination) : NavigationAction
    data object Back : NavigationAction
}