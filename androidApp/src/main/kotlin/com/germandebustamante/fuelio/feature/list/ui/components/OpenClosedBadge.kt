package com.germandebustamante.fuelio.feature.list.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing

@Composable
fun OpenClosedBadge(
    isOpen: Boolean,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor, stringRes) = if (isOpen) {
        Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            R.string.open,
        )
    } else {
        Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            R.string.closed,
        )
    }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .padding(horizontal = FuelioSpacing.xs, vertical = FuelioSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor),
        )
        Text(
            text = stringResource(stringRes),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}
