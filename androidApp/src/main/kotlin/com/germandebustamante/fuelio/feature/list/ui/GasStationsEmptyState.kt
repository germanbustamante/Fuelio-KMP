package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize
import com.germandebustamante.fuelio.designsystem.emptystate.FuelioEmptyState

/**
 * Provinces come from the backend and always have stations, and the fuel-type filter
 * never empties the list (it only swaps which price is shown per station) — so this
 * state is only reachable via a search with no name/address match.
 */
@Composable
fun GasStationsEmptyState(
    onChangeFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FuelioEmptyState(
        title = stringResource(R.string.empty_state_search_title),
        subtitle = stringResource(R.string.empty_state_search_subtitle),
        icon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        },
        action = {
            FuelioTextButton(
                text = stringResource(R.string.search_clear),
                onClick = onChangeFilters,
                config = TextButtonConfig(size = TextButtonSize.MEDIUM),
            )
        },
        modifier = modifier.testTag(A11yIdentifiers.EMPTY_STATE),
    )
}

@Preview(showBackground = true)
@Composable
private fun GasStationsEmptyStatePreview() {
    FuelioTheme {
        GasStationsEmptyState(
            onChangeFilters = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
