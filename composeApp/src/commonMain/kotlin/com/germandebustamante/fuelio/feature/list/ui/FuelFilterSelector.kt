package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.feature.list.state.FuelFilter
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.fuel_filter_diesel
import fuelio.composeapp.generated.resources.fuel_filter_diesel_premium
import fuelio.composeapp.generated.resources.fuel_filter_gasoline_95
import fuelio.composeapp.generated.resources.fuel_filter_gasoline_98
import org.jetbrains.compose.resources.stringResource

private val allFuelFilters = listOf(
    FuelFilter.Gasoline95,
    FuelFilter.Gasoline98,
    FuelFilter.Diesel,
    FuelFilter.DieselPremium,
)

@Composable
fun FuelFilterSelector(
    selectedFilter: FuelFilter,
    onFilterSelected: (FuelFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        allFuelFilters.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label()) },
            )
        }
    }
}

@Composable
private fun FuelFilter.label(): String = when (this) {
    is FuelFilter.Gasoline95 -> stringResource(Res.string.fuel_filter_gasoline_95)
    is FuelFilter.Gasoline98 -> stringResource(Res.string.fuel_filter_gasoline_98)
    is FuelFilter.Diesel -> stringResource(Res.string.fuel_filter_diesel)
    is FuelFilter.DieselPremium -> stringResource(Res.string.fuel_filter_diesel_premium)
}

@Preview(showBackground = true)
@Composable
private fun FuelFilterSelectorPreview() {
    FuelioTheme {
        FuelFilterSelector(
            selectedFilter = FuelFilter.Gasoline95,
            onFilterSelected = {},
        )
    }
}
