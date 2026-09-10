package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.designsystem.searchbar.FuelioSearchBar

@Composable
fun GasStationSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    FuelioSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = stringResource(R.string.search_gas_station),
        clearContentDescription = stringResource(R.string.search_clear),
        modifier = modifier
            .testTag(A11yIdentifiers.SEARCH_FIELD)
            .padding(horizontal = FuelioSpacing.md, vertical = FuelioSpacing.xs),
    )
}
