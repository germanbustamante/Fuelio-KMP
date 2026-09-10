package com.germandebustamante.fuelio.feature.favorites.state

import com.germandebustamante.fuelio.feature.list.state.GasStationItemVO

/**
 * Named `FavoritesContentState`, not `ContentState`, on purpose: the Objective-C exporter flattens
 * packages away, so the list's and the detail's `ContentState` already collide and come out as
 * `ContentState_` and `ContentState` (see `Support/KotlinSealed.swift`). A third one would add
 * another export-order-dependent name to that pile.
 */
sealed interface FavoritesContentState {
    data object Loading : FavoritesContentState

    // Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
    data class Success(val stations: List<GasStationItemVO>, val unresolvedCount: Int) : FavoritesContentState
    data object Empty : FavoritesContentState
}

// Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
data class FavoritesUIState(
    val stations: List<GasStationItemVO> = emptyList(),
    val unresolvedCount: Int = 0,
    val isLoading: Boolean = true,
) {
    /**
     * No error state: favourites are a local Room read with no network path, so there is nothing to
     * fail. `unresolvedCount` is not an error either — it is how many favourites exist whose station
     * isn't in the cache right now.
     */
    val contentState: FavoritesContentState
        get() = when {
            isLoading -> FavoritesContentState.Loading
            stations.isEmpty() -> FavoritesContentState.Empty
            else -> FavoritesContentState.Success(stations, unresolvedCount)
        }

    fun withFavoritesLoaded(stations: List<GasStationItemVO>, unresolvedCount: Int) =
        copy(stations = stations, unresolvedCount = unresolvedCount, isLoading = false)
}
