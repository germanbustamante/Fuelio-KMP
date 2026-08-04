package com.germandebustamante.fuelio.feature.list.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing

@Composable
fun CheapestBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.cheapest_station_tag),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = FuelioSpacing.xs, vertical = FuelioSpacing.xxs),
    )
}
