package com.germandebustamante.fuelio.feature.list.ui.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.designsystem.progress.FuelioGasStationItemSkeleton

@Composable
fun GasStationsLoadingSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = FuelioSpacing.md,
            end = FuelioSpacing.md,
            top = FuelioSpacing.sm,
            bottom = FuelioSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
        userScrollEnabled = false,
    ) {
        items(6) {
            FuelioGasStationItemSkeleton()
        }
    }
}
