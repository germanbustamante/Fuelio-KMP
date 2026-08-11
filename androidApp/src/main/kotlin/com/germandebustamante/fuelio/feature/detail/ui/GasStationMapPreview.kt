package com.germandebustamante.fuelio.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

// Static preview, not an interactive map: it sits inside a scrolling LazyColumn, and Maps' own
// pan/zoom gestures would otherwise steal the drag from the list's scroll. iOS makes the same call
// with `.allowsHitTesting(false)` on its MapKit view (see StationMapSection.swift) — tapping
// "Get directions" is how the user actually interacts with the map, on both platforms.
private val nonInteractiveMapUiSettings = MapUiSettings(
    zoomControlsEnabled = false,
    zoomGesturesEnabled = false,
    scrollGesturesEnabled = false,
    rotationGesturesEnabled = false,
    tiltGesturesEnabled = false,
    myLocationButtonEnabled = false,
    mapToolbarEnabled = false,
    compassEnabled = false,
)

private val nonInteractiveMapProperties = MapProperties(isIndoorEnabled = false)

@Composable
fun GasStationMapPreview(
    stationName: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
) {
    val markerState = rememberUpdatedMarkerState(position = LatLng(latitude, longitude))
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 16f)
    }
    val mapContentDescription = stringResource(R.string.detail_map_showing, stationName)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { contentDescription = mapContentDescription },
        contentAlignment = Alignment.Center,
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = nonInteractiveMapUiSettings,
            properties = nonInteractiveMapProperties,
        ) {
            Marker(
                state = markerState,
                title = stationName,
            )
        }
    }
}

@Composable
@Preview("LightMode", showBackground = true)
@Preview(name = "DarkMode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
private fun GasStationMapPreviewPreview() {
    val gasStation = fakeGasStations.first()
    FuelioTheme {
        GasStationMapPreview(
            stationName = gasStation.displayName,
            latitude = gasStation.latitude,
            longitude = gasStation.longitude,
        )
    }
}
