package com.germandebustamante.fuelio.feature.map.state

import com.germandebustamante.fuelio.core.domain.error.DomainError

/**
 * Named `MapContentState`, not `ContentState`: the Objective-C exporter flattens packages away, so
 * the list's and the detail's `ContentState` already collide (`ContentState_`/`ContentState` — see
 * `Support/KotlinSealed.swift`). A third feature-scoped sealed type keeps its own name here rather
 * than adding another export-order-dependent collision.
 */
sealed interface MapContentState {
    data object Loading : MapContentState

    // Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
    data class Success(val markers: List<MapMarkerVO>) : MapContentState
    data object Empty : MapContentState
    data class Error(val message: String) : MapContentState
}

// Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
data class MapUIState(val markers: List<MapMarkerVO> = emptyList(), val isLoading: Boolean = true, val error: DomainError? = null) {
    val contentState: MapContentState
        get() = when {
            error != null -> MapContentState.Error(error.message.orEmpty())
            isLoading -> MapContentState.Loading
            markers.isEmpty() -> MapContentState.Empty
            else -> MapContentState.Success(markers)
        }

    fun withMarkersLoaded(markers: List<MapMarkerVO>) = copy(markers = markers, isLoading = false, error = null)

    fun withError(error: DomainError) = copy(error = error, isLoading = false)
}
