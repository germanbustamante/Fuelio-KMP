package com.germandebustamante.fuelio.core.interop

import com.germandebustamante.fuelio.core.navigation.action.NavigationAction
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Single point of consumption for `Navigator.navigationActions`.
 *
 * That flow is backed by a `Channel`, so it has exactly one consumer: two subscribers would steal
 * events from each other and navigation would drop silently. Exactly one of these must exist, owned
 * by the app root for the whole app lifetime.
 */
class IosNavigationBinding internal constructor(
    private val navigator: Navigator,
    private val context: CoroutineContext = Dispatchers.Main.immediate,
) {
    private val scope = CoroutineScope(context + SupervisorJob())
    private var subscription: FlowSubscription? = null

    fun observeNavigation(onEach: (NavigationAction) -> Unit): FlowSubscription {
        subscription?.cancel()
        return navigator.navigationActions.subscribe(context, onEach).also { subscription = it }
    }

    /**
     * Fire-and-forget navigation request. `Navigator.navigate` is `suspend`, which the exporter turns
     * into an `async throws` Swift function; this keeps the coroutine on the Kotlin side.
     *
     * Screens must **not** call this — they call a ViewModel action, so the matching analytics event
     * still fires. It exists for the Swift tests that drive an isolated navigator.
     */
    fun requestNavigation(destination: Destination) {
        scope.launch { navigator.navigate(destination) }
    }

    fun close() {
        subscription?.cancel()
        subscription = null
        scope.cancel()
    }
}
