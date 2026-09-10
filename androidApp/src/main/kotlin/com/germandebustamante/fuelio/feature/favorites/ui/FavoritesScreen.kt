package com.germandebustamante.fuelio.feature.favorites.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.emptystate.FuelioEmptyState
import com.germandebustamante.fuelio.designsystem.progress.FuelioLoadingIndicator
import com.germandebustamante.fuelio.designsystem.scaffold.FuelioScaffold
import com.germandebustamante.fuelio.designsystem.topbar.FuelioTopBar
import com.germandebustamante.fuelio.feature.favorites.state.FavoritesContentState
import com.germandebustamante.fuelio.feature.favorites.state.FavoritesUIState
import com.germandebustamante.fuelio.feature.favorites.state.FavoritesViewModel
import com.germandebustamante.fuelio.feature.list.state.GasStationItemVO
import com.germandebustamante.fuelio.feature.list.ui.GasStationItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier, viewModel: FavoritesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FavoritesScreen(
        state = state,
        onItemClick = viewModel::onItemClick,
        onToggleFavorite = viewModel::onToggleFavorite,
        onBackTapped = viewModel::onBackTapped,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreen(
    state: FavoritesUIState,
    onItemClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onBackTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FuelioScaffold(
        modifier = modifier.testTag(A11yIdentifiers.FAVORITES_SCREEN),
        topBar = {
            FuelioTopBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackTapped,
                        modifier = Modifier.testTag(A11yIdentifiers.FAVORITES_BACK_BUTTON),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.favorites_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val content = state.contentState) {
                FavoritesContentState.Loading -> FuelioLoadingIndicator(modifier = Modifier.align(Alignment.Center))

                FavoritesContentState.Empty -> FuelioEmptyState(
                    title = stringResource(R.string.favorites_empty_title),
                    subtitle = stringResource(R.string.favorites_empty_subtitle),
                    modifier = Modifier.align(Alignment.Center).testTag(A11yIdentifiers.FAVORITES_EMPTY),
                )

                is FavoritesContentState.Success -> FavoritesList(
                    stations = content.stations,
                    unresolvedCount = content.unresolvedCount,
                    onItemClick = onItemClick,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }
    }
}

@Composable
private fun FavoritesList(
    stations: List<GasStationItemVO>,
    unresolvedCount: Int,
    onItemClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(FuelioSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
    ) {
        if (unresolvedCount > 0) {
            item {
                // Favourites whose province isn't cached right now can't be rendered — say so rather
                // than looking like the app lost them.
                Text(
                    text = pluralStringResource(R.plurals.favorites_unresolved_banner, unresolvedCount, unresolvedCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(A11yIdentifiers.FAVORITES_UNRESOLVED_BANNER),
                )
            }
        }

        items(items = stations, key = { it.station.id }) { station ->
            GasStationItem(
                gasStation = station,
                isFavorite = true,
                onItemClick = { onItemClick(station.station.id) },
                onToggleFavorite = { onToggleFavorite(station.station.id) },
            )
        }
    }
}

@Preview("LightMode", showBackground = true)
@Preview(name = "DarkMode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun FavoritesScreenEmptyPreview() {
    FuelioTheme {
        FavoritesScreen(
            state = FavoritesUIState(isLoading = false),
            onItemClick = {},
            onToggleFavorite = {},
            onBackTapped = {},
        )
    }
}
