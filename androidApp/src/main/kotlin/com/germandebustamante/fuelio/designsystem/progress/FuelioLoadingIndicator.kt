package com.germandebustamante.fuelio.designsystem.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

enum class FuelioIndicatorSize(val size: Dp) { Small(20.dp), Medium(32.dp), Large(48.dp) }

@Composable
fun FuelioLoadingIndicator(modifier: Modifier = Modifier, size: FuelioIndicatorSize = FuelioIndicatorSize.Medium) {
    CircularProgressIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = when (size) {
            FuelioIndicatorSize.Small -> 2.dp
            FuelioIndicatorSize.Medium -> 3.dp
            FuelioIndicatorSize.Large -> 4.dp
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun FuelioLoadingIndicatorPreview() {
    FuelioTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            FuelioLoadingIndicator(size = FuelioIndicatorSize.Small)
            FuelioLoadingIndicator(size = FuelioIndicatorSize.Medium)
            FuelioLoadingIndicator(size = FuelioIndicatorSize.Large)
        }
    }
}
