package com.germandebustamante.fuelio.feature.settings.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.chip.FuelioFilterChip
import com.germandebustamante.fuelio.feature.list.state.toFuelFilter
import com.germandebustamante.fuelio.feature.list.ui.rawValue

@Composable
fun DefaultFuelSelector(selectedFuelType: FuelType, onFuelTypeSelected: (FuelType) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag(A11yIdentifiers.DEFAULT_FUEL_PICKER)
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
    ) {
        FuelType.entries.forEach { fuelType ->
            FuelioFilterChip(
                selected = fuelType == selectedFuelType,
                onClick = { onFuelTypeSelected(fuelType) },
                label = fuelType.label(),
                // Reuses the list screen's fuel identifiers: the same four fuels, so the UI tests
                // locate them the same way on both screens.
                modifier = Modifier.testTag(A11yIdentifiers.fuelOption(fuelType.toFuelFilter().rawValue)),
            )
        }
    }
}

@Composable
private fun FuelType.label(): String = when (this) {
    FuelType.GASOLINE_95 -> stringResource(R.string.fuel_filter_gasoline_95)
    FuelType.GASOLINE_98 -> stringResource(R.string.fuel_filter_gasoline_98)
    FuelType.DIESEL -> stringResource(R.string.fuel_filter_diesel)
    FuelType.DIESEL_PREMIUM -> stringResource(R.string.fuel_filter_diesel_premium)
}

@Preview(showBackground = true)
@Composable
private fun DefaultFuelSelectorPreview() {
    FuelioTheme {
        DefaultFuelSelector(selectedFuelType = FuelType.DIESEL, onFuelTypeSelected = {})
    }
}
