package com.germandebustamante.fuelio.core.interop

import com.germandebustamante.fuelio.core.navigation.action.NavigationAction
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import kotlinx.coroutines.Dispatchers
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
    private var subscription: FlowSubscription? = null

    fun observeNavigation(onEach: (NavigationAction) -> Unit): FlowSubscription {
        subscription?.cancel()
        return navigator.navigationActions.subscribe(context, onEach).also { subscription = it }
    }

    fun close() {
        subscription?.cancel()
        subscription = null
    }
}
