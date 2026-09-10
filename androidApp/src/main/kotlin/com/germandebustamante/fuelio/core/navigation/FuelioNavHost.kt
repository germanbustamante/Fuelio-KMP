package com.germandebustamante.fuelio.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.germandebustamante.fuelio.core.flow.ObserveAsEvent
import com.germandebustamante.fuelio.core.navigation.action.NavigationAction
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.deeplink.ExternalUriHandler
import com.germandebustamante.fuelio.core.navigation.deeplink.parseDeepLink
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.core.navigation.destination.DestinationNavKey
import com.germandebustamante.fuelio.core.navigation.destination.buildSyntheticBackStack
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.detail.ui.GasStationDetail
import com.germandebustamante.fuelio.feature.list.ui.GasStationsScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject

@Composable
fun FuelioNavHost(locationPermissionController: LocationPermissionController) {
    val navigator = koinInject<Navigator>()
    val backStack = rememberNavBackStack(navBackStackConfig, DestinationNavKey(Destination.GasStations))

    ObserveAsEvent(flow = navigator.navigationActions) { action ->
        when (action) {
            is NavigationAction.Navigate -> backStack.add(DestinationNavKey(action.destination))
            is NavigationAction.Back -> backStack.removeLastOrNull()
        }
    }

    DisposableEffect(Unit) {
        ExternalUriHandler.listener = { uri ->
            parseDeepLink(uri)?.let { destination ->
                backStack.clear()
                backStack.addAll(buildSyntheticBackStack(destination).map { DestinationNavKey(it) })
            }
        }
        onDispose { ExternalUriHandler.listener = null }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<DestinationNavKey> { key ->
                when (val destination = key.destination) {
                    is Destination.GasStations ->
                        GasStationsScreen(locationPermissionController, modifier = Modifier.fillMaxSize())
                    is Destination.GasStationDetails -> GasStationDetail(destination)
                }
            }
        },
    )
}

@OptIn(ExperimentalSerializationApi::class)
private val navBackStackConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(DestinationNavKey::class)
        }
    }
}
