package com.germandebustamante.fuelio.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.flow.ObserveAsEvent
import com.germandebustamante.fuelio.core.navigation.action.NavigationAction
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.deeplink.ExternalUriHandler
import com.germandebustamante.fuelio.core.navigation.deeplink.parseDeepLink
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.core.navigation.destination.DestinationNavKey
import com.germandebustamante.fuelio.core.navigation.destination.buildSyntheticBackStack
import com.germandebustamante.fuelio.feature.common.analytics.DeepLinkOpened
import com.germandebustamante.fuelio.feature.detail.ui.GasStationDetail
import com.germandebustamante.fuelio.feature.favorites.ui.FavoritesScreen
import com.germandebustamante.fuelio.feature.list.ui.GasStationsScreen
import com.germandebustamante.fuelio.feature.map.ui.MapScreen
import com.germandebustamante.fuelio.feature.settings.ui.SettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject

@Composable
fun FuelioNavHost() {
    val navigator = koinInject<Navigator>()
    val analyticsManager = koinInject<AnalyticsTracking>()
    val coroutineScope = rememberCoroutineScope()
    val backStack = rememberNavBackStack(navBackStackConfig, DestinationNavKey(Destination.GasStations))

    ObserveAsEvent(flow = navigator.navigationActions) { action ->
        when (action) {
            is NavigationAction.Navigate -> backStack.add(DestinationNavKey(action.destination))
            is NavigationAction.Back -> backStack.removeLastOrNull()
        }
    }

    DisposableEffect(Unit) {
        ExternalUriHandler.listener = { uri ->
            val destination = parseDeepLink(uri)
            trackDeepLink(coroutineScope, analyticsManager, uri, resolved = destination != null)
            destination?.let {
                backStack.clear()
                backStack.addAll(buildSyntheticBackStack(it).map { key -> DestinationNavKey(key) })
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
                        GasStationsScreen(modifier = Modifier.fillMaxSize())
                    is Destination.GasStationDetails -> GasStationDetail(destination)
                    is Destination.Settings -> SettingsScreen()
                    is Destination.Favorites -> FavoritesScreen()
                    is Destination.Map -> MapScreen()
                }
            }
        },
    )
}

private fun trackDeepLink(scope: CoroutineScope, analyticsManager: AnalyticsTracking, uri: String, resolved: Boolean) {
    scope.launch { analyticsManager.track(DeepLinkOpened(uri, resolved)) }
}

@OptIn(ExperimentalSerializationApi::class)
private val navBackStackConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(DestinationNavKey::class)
        }
    }
}
