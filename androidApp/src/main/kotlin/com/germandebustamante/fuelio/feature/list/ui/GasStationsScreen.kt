@file:OptIn(ExperimentalMaterial3Api::class)

package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioIconButton
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonSize
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonVariant
import com.germandebustamante.fuelio.designsystem.divider.FuelioDivider
import com.germandebustamante.fuelio.designsystem.scaffold.FuelioScaffold
import com.germandebustamante.fuelio.designsystem.topbar.FuelioTopBar
import com.germandebustamante.fuelio.designsystem.topbar.FuelioTopBarVariant
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.list.state.ContentState
import com.germandebustamante.fuelio.feature.list.state.FuelFilter
import com.germandebustamante.fuelio.feature.list.state.GasStationsUIState
import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIState
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStateError
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStateLoading
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStatePermissionSnackbar
import com.germandebustamante.fuelio.feature.list.state.fakeGasStationsUIStateShowModalSheet
import com.germandebustamante.fuelio.feature.list.ui.state.GasStationsErrorState
import com.germandebustamante.fuelio.feature.list.ui.state.GasStationsLoadingSkeleton
import com.germandebustamante.fuelio.R
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
        onOpenAppSettings = viewModel::onOpenAppSettings,
        onDismissPermissionSnackbar = viewModel::onDismissPermissionSnackbar,
        onDismissStaleDataError = viewModel::onDismissStaleDataError,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onItemClick = viewModel::onItemClick,
        onToggleFavorite = viewModel::onToggleFavorite,
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
    onOpenAppSettings: () -> Unit,
    onDismissPermissionSnackbar: () -> Unit,
    onDismissStaleDataError: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onItemClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    val showFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 3 } }
    val scope = rememberCoroutineScope()

    FuelioScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FuelioTopBar(
                variant = FuelioTopBarVariant.Small,
                scrollBehavior = scrollBehavior,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(MaterialTheme.shapes.small),
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            state.selectedProvince?.let { province ->
                                Row(
                                    modifier = Modifier
                                        .testTag(A11yIdentifiers.PROVINCE_BUTTON)
                                        .clickable { onFilterProvinceToggle(true) }
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.xxs),
                                ) {
                                    Text(
                                        text = province.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = stringResource(R.string.top_bar_subtitle_change_province),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    FuelioIconButton(
                        onClick = onDetectLocationTapped,
                        drawableRes = R.drawable.my_location_ic,
                        config = IconButtonConfig(size = IconButtonSize.MEDIUM, variant = IconButtonVariant.Standard),
                        contentDescriptionRes = R.string.detect_location,
                        modifier = Modifier.testTag(A11yIdentifiers.DETECT_LOCATION_BUTTON),
                    )
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
            ) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.scroll_to_top),
                        modifier = Modifier.padding(FuelioSpacing.xxs),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GasStationSearchBar(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChanged,
                )
                FuelFilterSelector(
                    selectedFilter = state.selectedFuelFilter,
                    onFilterSelected = onFuelFilterSelected,
                )
                FuelioDivider()

                AnimatedContent(
                    targetState = state.contentState,
                    label = "content_state",
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) { contentState ->
                    when (contentState) {
                        ContentState.Initial,
                        ContentState.Loading -> GasStationsLoadingSkeleton()

                        is ContentState.Success -> LazyColumn(
                            state = listState,
                            modifier = Modifier.testTag(A11yIdentifiers.STATIONS_LIST),
                            contentPadding = PaddingValues(
                                start = FuelioSpacing.md,
                                end = FuelioSpacing.md,
                                top = FuelioSpacing.sm,
                                bottom = FuelioSpacing.md,
                            ),
                            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
                        ) {
                            items(contentState.stations, key = { it.station.id }) { station ->
                                GasStationItem(
                                    gasStation = station,
                                    isFavorite = station.station.id in state.favorites,
                                    onItemClick = { onItemClick(station.station.id) },
                                    onToggleFavorite = { onToggleFavorite(station.station.id) },
                                )
                            }
                        }

                        ContentState.Empty -> GasStationsEmptyState(
                            onChangeFilters = { onSearchQueryChanged("") },
                            modifier = Modifier.fillMaxSize(),
                        )

                        is ContentState.Error -> GasStationsErrorState(
                            message = contentState.message,
                            onRetry = onRetry,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }

    val permissionSnackbarMessage = stringResource(R.string.location_permission_snackbar_message)
    val permissionSnackbarAction = stringResource(R.string.location_permission_open_settings)
    LaunchedEffect(state.showPermissionDeniedPermanentlySnackbar) {
        if (state.showPermissionDeniedPermanentlySnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = permissionSnackbarMessage,
                actionLabel = permissionSnackbarAction,
                duration = SnackbarDuration.Long,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> onOpenAppSettings()
                SnackbarResult.Dismissed -> onDismissPermissionSnackbar()
            }
        }
    }

    val staleDataSnackbarMessage = stringResource(R.string.stale_data_snackbar_message)
    LaunchedEffect(state.staleDataError) {
        if (state.staleDataError != null) {
            snackbarHostState.showSnackbar(
                message = staleDataSnackbarMessage,
                duration = SnackbarDuration.Short,
            )
            onDismissStaleDataError()
        }
    }

    ProvinceBottomSheetDialog(
        provinces = state.provinces,
        onProvinceSelected = onProvinceSelected,
        onDismissRequest = { onFilterProvinceToggle(false) },
        showBottomSheet = state.showFilterProvince,
    )
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
    var provinceSearchQuery by rememberSaveable { mutableStateOf("") }
    val filteredProvinces = remember(provinces, provinceSearchQuery) {
        if (provinceSearchQuery.isBlank()) provinces
        else provinces.filter { it.name.contains(provinceSearchQuery, ignoreCase = true) }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                provinceSearchQuery = ""
                onDismissRequest()
            },
            sheetState = bottomSheetState,
            modifier = modifier.testTag(A11yIdentifiers.PROVINCE_SHEET),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FuelioSpacing.md),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = FuelioSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.select_province),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    FuelioIconButton(
                        drawableRes = R.drawable.close_ic,
                        config = IconButtonConfig(size = IconButtonSize.SMALL, variant = IconButtonVariant.Standard),
                        contentDescriptionRes = R.string.province_sheet_close,
                        modifier = Modifier.testTag(A11yIdentifiers.PROVINCE_SHEET_CLOSE),
                        onClick = {
                            provinceSearchQuery = ""
                            onDismissRequest()
                        },
                    )
                }

                OutlinedTextField(
                    value = provinceSearchQuery,
                    onValueChange = { provinceSearchQuery = it },
                    placeholder = { Text(stringResource(R.string.province_search_placeholder)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = FuelioSpacing.sm),
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(
                    start = FuelioSpacing.md,
                    end = FuelioSpacing.md,
                    bottom = FuelioSpacing.md,
                ),
            ) {
                items(filteredProvinces, key = { it.id }) { province ->
                    ProvinceItem(
                        name = province.name,
                        onClick = {
                            provinceSearchQuery = ""
                            onProvinceSelected(province)
                            onDismissRequest()
                        },
                        modifier = Modifier.testTag(A11yIdentifiers.provinceRow(province.id)),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProvinceItem(name: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = FuelioSpacing.md),
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
            onOpenAppSettings = {},
            onDismissPermissionSnackbar = {},
            onDismissStaleDataError = {},
            onRefresh = {},
            onRetry = {},
            onItemClick = {},
            onToggleFavorite = {},
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
            onOpenAppSettings = {},
            onDismissPermissionSnackbar = {},
            onDismissStaleDataError = {},
            onRefresh = {},
            onRetry = {},
            onItemClick = {},
            onToggleFavorite = {},
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
            onOpenAppSettings = {},
            onDismissPermissionSnackbar = {},
            onDismissStaleDataError = {},
            onRefresh = {},
            onRetry = {},
            onItemClick = {},
            onToggleFavorite = {},
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
            onOpenAppSettings = {},
            onDismissPermissionSnackbar = {},
            onDismissStaleDataError = {},
            onRefresh = {},
            onRetry = {},
            onItemClick = {},
            onToggleFavorite = {},
        )
    }
}

@Preview("Permission Denied Permanently (Snackbar)", showBackground = true)
@Composable
private fun GasStationsScreenPermissionSnackbarPreview() {
    FuelioTheme {
        GasStationsScreen(
            state = fakeGasStationsUIStatePermissionSnackbar,
            onFilterProvinceToggle = {},
            onProvinceSelected = {},
            onFuelFilterSelected = {},
            onSearchQueryChanged = {},
            onDismissError = {},
            onDetectLocationTapped = {},
            onOpenAppSettings = {},
            onDismissPermissionSnackbar = {},
            onDismissStaleDataError = {},
            onRefresh = {},
            onRetry = {},
            onItemClick = {},
            onToggleFavorite = {},
        )
    }
}
