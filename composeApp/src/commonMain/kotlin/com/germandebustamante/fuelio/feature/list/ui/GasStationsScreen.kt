@file:OptIn(ExperimentalMaterial3Api::class)

package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.feature.list.state.FuelFilter
import com.germandebustamante.fuelio.designsystem.button.FuelioIconButton
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonSize
import com.germandebustamante.fuelio.feature.common.dialog.error.ErrorDialog
import com.germandebustamante.fuelio.feature.common.dialog.permission.LocationPermissionDialog
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import com.germandebustamante.fuelio.feature.list.state.GasStationsUIState
import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIState
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStateError
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStateLoading
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStatePermissionDenied
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStatePermissionDeniedAlways
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStateShowModalSheet
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.app_name
import fuelio.composeapp.generated.resources.close_ic
import fuelio.composeapp.generated.resources.detect_location
import fuelio.composeapp.generated.resources.gas_station_ic
import fuelio.composeapp.generated.resources.my_location_ic
import fuelio.composeapp.generated.resources.select_province
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GasStationsScreen(
    locationPermissionController: LocationPermissionController,
    viewModel: GasStationsViewModel = koinViewModel { parametersOf(locationPermissionController) },
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GasStationsScreen(
        state = state,
        onFilterProvinceToggle = viewModel::onFilterProvinceToggle,
        onProvinceSelected = viewModel::onProvinceSelected,
        onFuelFilterSelected = viewModel::onFuelFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDismissError = viewModel::onDismissError,
        onDetectLocationTapped = viewModel::onDetectLocationTapped,
        onPermissionRationaleAccepted = viewModel::onPermissionRationaleAccepted,
        onOpenAppSettings = viewModel::onOpenAppSettings,
        onPermissionDialogDismissed = viewModel::onPermissionDialogDismissed,
        modifier = modifier,
    )
}

@Composable
private fun GasStationsScreen(
    state: GasStationsUIState,
    onDismissError: () -> Unit,
    onFilterProvinceToggle: (Boolean) -> Unit,
    onProvinceSelected: (ProvinceBO) -> Unit,
    onFuelFilterSelected: (FuelFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDetectLocationTapped: () -> Unit,
    onPermissionRationaleAccepted: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onPermissionDialogDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        if (state.isContentReady()) {
            GasStationsContent(
                state = state,
                onFilterProvinceToggle = onFilterProvinceToggle,
                onProvinceSelected = onProvinceSelected,
                onFuelFilterSelected = onFuelFilterSelected,
                onSearchQueryChanged = onSearchQueryChanged,
                onDetectLocationTapped = onDetectLocationTapped,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        state.error?.let { error ->
            ErrorDialog(
                description = error.message.toString(),
                onDismissRequest = onDismissError,
            )
        }

        state.locationPermissionState?.let { permissionState ->
            LocationPermissionDialog(
                permissionState = permissionState,
                onPrimaryAction = when (permissionState) {
                    LocationPermissionState.DeniedAlways -> onOpenAppSettings
                    else -> onPermissionRationaleAccepted
                },
                onDismiss = onPermissionDialogDismissed,
            )
        }
    }
}

@Composable
private fun GasStationsContent(
    state: GasStationsUIState,
    onFilterProvinceToggle: (Boolean) -> Unit,
    onProvinceSelected: (ProvinceBO) -> Unit,
    onFuelFilterSelected: (FuelFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDetectLocationTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.gas_station_ic),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.small)
                    .padding(8.dp),
            )

            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(Modifier.weight(1f))

            IconButton(onClick = onDetectLocationTapped) {
                Icon(
                    painter = painterResource(Res.drawable.my_location_ic),
                    contentDescription = stringResource(Res.string.detect_location),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            state.selectedProvince?.let {
                ProvinceFilterButton(province = it, onClick = { onFilterProvinceToggle(true) })
            }
        }

        GasStationSearchBar(onQueryChange = onSearchQueryChanged)

        FuelFilterSelector(
            selectedFilter = state.selectedFuelFilter,
            onFilterSelected = onFuelFilterSelected,
        )

        HorizontalDivider()

        if (state.gasStations.isEmpty()) {
            GasStationsEmptyState(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(state.gasStations, key = { it.station.id }) {
                    GasStationItem(it)
                }
            }
        }

        ProvinceBottomSheetDialog(
            provinces = state.provinces,
            onProvinceSelected = onProvinceSelected,
            onDismissRequest = { onFilterProvinceToggle(false) },
            showBottomSheet = state.showFilterProvince,
        )
    }
}

@Composable
private fun ProvinceBottomSheetDialog(
    provinces: List<ProvinceBO>,
    onProvinceSelected: (ProvinceBO) -> Unit,
    onDismissRequest: () -> Unit,
    showBottomSheet: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val bottomSheetState = rememberModalBottomSheetState()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = bottomSheetState,
            modifier = modifier,
        ) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.select_province),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                        )

                        FuelioIconButton(
                            drawableRes = Res.drawable.close_ic,
                            config = IconButtonConfig(size = IconButtonSize.SMALL),
                            onClick = onDismissRequest,
                        )
                    }
                }

                items(provinces, key = { it.id }) { province ->
                    ProvinceItem(
                        name = province.name,
                        onClick = {
                            onProvinceSelected(province)
                            onDismissRequest()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProvinceItem(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview("LightMode", showBackground = true)
@Preview(name = "DarkMode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun GasStationsScreenPreview() {
    FuelioTheme {
        GasStationsScreen(
            state = fakeGasStationsUIState,
            onFilterProvinceToggle = {},
            onProvinceSelected = {},
            onFuelFilterSelected = {},
            onSearchQueryChanged = {},
            onDismissError = {},
            onDetectLocationTapped = {},
            onPermissionRationaleAccepted = {},
            onOpenAppSettings = {},
            onPermissionDialogDismissed = {},
        )
    }
}

@Preview
@Composable
private fun GasStationWithProvincesModalOpenedPreview() {
    FuelioTheme {
        GasStationsScreen(
            state = fakeGasStationsUIStateShowModalSheet,
            onFilterProvinceToggle = {},
            onProvinceSelected = {},
            onFuelFilterSelected = {},
            onSearchQueryChanged = {},
            onDismissError = {},
            onDetectLocationTapped = {},
            onPermissionRationaleAccepted = {},
            onOpenAppSettings = {},
            onPermissionDialogDismissed = {},
        )
    }
}

@Preview("Loading", showBackground = true)
@Composable
private fun GasStationsScreenLoadingPreview() {
    FuelioTheme {
        GasStationsScreen(
            state = fakeGasStationsUIStateLoading,
            onFilterProvinceToggle = {},
            onProvinceSelected = {},
            onFuelFilterSelected = {},
            onSearchQueryChanged = {},
            onDismissError = {},
            onDetectLocationTapped = {},
            onPermissionRationaleAccepted = {},
            onOpenAppSettings = {},
            onPermissionDialogDismissed = {},
        )
    }
}

@Preview("Error", showBackground = true)
@Composable
private fun GasStationsScreenErrorPreview() {
    FuelioTheme {
        GasStationsScreen(
            state = fakeGasStationsUIStateError,
            onFilterProvinceToggle = {},
            onProvinceSelected = {},
            onFuelFilterSelected = {},
            onSearchQueryChanged = {},
            onDismissError = {},
            onDetectLocationTapped = {},
            onPermissionRationaleAccepted = {},
            onOpenAppSettings = {},
            onPermissionDialogDismissed = {},
        )
    }
}

@Preview("Permission Denied", showBackground = true)
@Composable
private fun GasStationsScreenPermissionDeniedPreview() {
    FuelioTheme {
        GasStationsScreen(
            state = fakeGasStationsUIStatePermissionDenied,
            onFilterProvinceToggle = {},
            onProvinceSelected = {},
            onFuelFilterSelected = {},
            onSearchQueryChanged = {},
            onDismissError = {},
            onDetectLocationTapped = {},
            onPermissionRationaleAccepted = {},
            onOpenAppSettings = {},
            onPermissionDialogDismissed = {},
        )
    }
}

@Preview("Permission Denied Always", showBackground = true)
@Composable
private fun GasStationsScreenPermissionDeniedAlwaysPreview() {
    FuelioTheme {
        GasStationsScreen(
            state = fakeGasStationsUIStatePermissionDeniedAlways,
            onFilterProvinceToggle = {},
            onProvinceSelected = {},
            onFuelFilterSelected = {},
            onSearchQueryChanged = {},
            onDismissError = {},
            onDetectLocationTapped = {},
            onPermissionRationaleAccepted = {},
            onOpenAppSettings = {},
            onPermissionDialogDismissed = {},
        )
    }
}
