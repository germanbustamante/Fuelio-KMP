package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.designsystem.searchbar.FuelioSearchBar
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.search_clear
import fuelio.composeapp.generated.resources.search_gas_station
import org.jetbrains.compose.resources.stringResource

@Composable
fun GasStationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FuelioSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = stringResource(Res.string.search_gas_station),
        clearContentDescription = stringResource(Res.string.search_clear),
        modifier = modifier.padding(horizontal = FuelioSpacing.md, vertical = FuelioSpacing.xs),
    )
}
