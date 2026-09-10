package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.chip.FuelioFilterChip
import com.germandebustamante.fuelio.feature.list.state.FuelFilter

private val allFuelFilters = listOf(
    FuelFilter.Gasoline95,
    FuelFilter.Gasoline98,
    FuelFilter.Diesel,
    FuelFilter.DieselPremium,
)

@Composable
fun FuelFilterSelector(selectedFilter: FuelFilter, onFilterSelected: (FuelFilter) -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .testTag(A11yIdentifiers.FUEL_FILTER_PICKER)
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = FuelioSpacing.md, vertical = FuelioSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
    ) {
        allFuelFilters.forEach { filter ->
            FuelioFilterChip(
                selected = filter == selectedFilter,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onFilterSelected(filter)
                },
                label = filter.label(),
                modifier = Modifier.testTag(A11yIdentifiers.fuelOption(filter.rawValue)),
            )
        }
    }
}

@Composable
private fun FuelFilter.label(): String = when (this) {
    is FuelFilter.Gasoline95 -> stringResource(R.string.fuel_filter_gasoline_95)
    is FuelFilter.Gasoline98 -> stringResource(R.string.fuel_filter_gasoline_98)
    is FuelFilter.Diesel -> stringResource(R.string.fuel_filter_diesel)
    is FuelFilter.DieselPremium -> stringResource(R.string.fuel_filter_diesel_premium)
}

/**
 * Matches iOS's `FuelKind.rawValue` so `A11yIdentifiers.fuelOption(...)` builds the same tag.
 *
 * `internal` rather than private so the settings screen's default-fuel picker reuses the same
 * identifiers — same four fuels, so the UI tests locate them the same way on both screens.
 */
internal val FuelFilter.rawValue: String
    get() = when (this) {
        is FuelFilter.Gasoline95 -> "gasoline95"
        is FuelFilter.Gasoline98 -> "gasoline98"
        is FuelFilter.Diesel -> "diesel"
        is FuelFilter.DieselPremium -> "dieselPremium"
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
