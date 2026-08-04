package com.germandebustamante.fuelio.core.navigation.destination

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * [Destination] lives in `:core:presentation`, which must not depend on Compose/Navigation3.
 * This wrapper is the only place in the app that couples a [Destination] to [NavKey], so the
 * back stack (owned by [com.germandebustamante.fuelio.core.navigation.FuelioNavHost]) stays typed.
 */
@Serializable
data class DestinationNavKey(val destination: Destination) : NavKey
