package com.germandebustamante.fuelio.feature.map.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.core.util.formatAsEuros
import com.germandebustamante.fuelio.designsystem.emptystate.FuelioEmptyState
import com.germandebustamante.fuelio.designsystem.progress.FuelioLoadingIndicator
import com.germandebustamante.fuelio.designsystem.scaffold.FuelioScaffold
import com.germandebustamante.fuelio.designsystem.topbar.FuelioTopBar
import com.germandebustamante.fuelio.feature.map.state.MapContentState
import com.germandebustamante.fuelio.feature.map.state.MapMarkerVO
import com.germandebustamante.fuelio.feature.map.state.MapUIState
import com.germandebustamante.fuelio.feature.map.state.MapViewModel
import com.germandebustamante.fuelio.feature.map.state.toMapMarkerVO
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.compose.viewmodel.koinViewModel

private data class StationClusterItem(val marker: MapMarkerVO) : ClusterItem {
    override val position: LatLng get() = LatLng(marker.latitude, marker.longitude)
    override val title: String get() = marker.displayName
    override val snippet: String? get() = marker.cheapestPrice?.formatAsEuros()
    override val zIndex: Float get() = 0f
}

@Composable
fun MapScreen(modifier: Modifier = Modifier, viewModel: MapViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MapScreen(
        state = state,
        onBackClick = viewModel::onBackClick,
        onMarkerSelected = viewModel::onMarkerSelected,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapScreen(state: MapUIState, onBackClick: () -> Unit, onMarkerSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    FuelioScaffold(
        modifier = modifier.testTag(A11yIdentifiers.MAP_SCREEN),
        topBar = {
            FuelioTopBar(
                title = { Text(stringResource(R.string.map_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag(A11yIdentifiers.MAP_BACK_BUTTON),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.map_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val content = state.contentState) {
                MapContentState.Loading -> FuelioLoadingIndicator(modifier = Modifier.align(Alignment.Center))

                MapContentState.Empty -> FuelioEmptyState(
                    title = stringResource(R.string.map_empty_title),
                    subtitle = stringResource(R.string.map_empty_subtitle),
                    modifier = Modifier.align(Alignment.Center),
                )

                is MapContentState.Error -> FuelioEmptyState(
                    title = stringResource(R.string.error_state_title),
                    subtitle = content.message.ifBlank { null },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    modifier = Modifier.align(Alignment.Center),
                )

                is MapContentState.Success -> StationsMap(markers = content.markers, onMarkerSelected = onMarkerSelected)
            }
        }
    }
}

@Composable
private fun StationsMap(markers: List<MapMarkerVO>, onMarkerSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val first = markers.first()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(first.latitude, first.longitude), 11f)
    }
    val items = markers.map(::StationClusterItem)

    GoogleMap(modifier = modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
        Clustering(
            items = items,
            onClusterItemClick = { item ->
                onMarkerSelected(item.marker.gasStationId)
                true
            },
        )
    }
}

@Preview("LightMode", showBackground = true)
@Preview(name = "DarkMode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun MapScreenPreview() {
    FuelioTheme {
        MapScreen(
            state = MapUIState(markers = fakeGasStations.map { it.toMapMarkerVO() }, isLoading = false),
            onBackClick = {},
            onMarkerSelected = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun MapScreenEmptyPreview() {
    FuelioTheme {
        MapScreen(state = MapUIState(isLoading = false), onBackClick = {}, onMarkerSelected = {})
    }
}
