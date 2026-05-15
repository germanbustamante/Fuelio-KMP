package com.germandebustamante.fuelio.designsystem.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

@Composable
fun FuelioFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = leadingIcon,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    )
}

@Composable
fun FuelioAssistChip(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    leadingPainter: Painter? = null,
    leadingPainterContentDescription: String? = null,
    enabled: Boolean = true,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = when {
            leadingIcon != null -> {
                { Icon(imageVector = leadingIcon, contentDescription = leadingIconContentDescription) }
            }
            leadingPainter != null -> {
                { Icon(painter = leadingPainter, contentDescription = leadingPainterContentDescription) }
            }
            else -> null
        },
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun FuelioFilterChipPreview() {
    FuelioTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            FuelioFilterChip(selected = false, onClick = {}, label = "Unselected")
            FuelioFilterChip(selected = true, onClick = {}, label = "Selected")
            FuelioFilterChip(selected = false, onClick = {}, label = "Disabled", enabled = false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FuelioAssistChipPreview() {
    FuelioTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            FuelioAssistChip(onClick = {}, label = "No icon")
            FuelioAssistChip(
                onClick = {},
                label = "With icon",
                leadingIcon = Icons.Outlined.Star,
                leadingIconContentDescription = null,
            )
        }
    }
}
