package com.germandebustamante.fuelio.designsystem.divider

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

@Composable
fun FuelioDivider(modifier: Modifier = Modifier, thickness: Dp = 1.dp) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun FuelioDividerPreview() {
    FuelioTheme {
        Column(modifier = Modifier.padding(FuelioSpacing.md)) {
            Text("Above divider")
            FuelioDivider()
            Text("Below divider")
            FuelioDivider(thickness = 2.dp)
            Text("Below thick divider")
        }
    }
}
