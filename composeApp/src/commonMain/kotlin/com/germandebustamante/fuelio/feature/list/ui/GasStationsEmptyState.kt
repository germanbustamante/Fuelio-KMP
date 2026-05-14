package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.empty_state_subtitle
import fuelio.composeapp.generated.resources.empty_state_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GasStationsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text(text = "😔", fontSize = 56.sp)

        Text(
            text = stringResource(Res.string.empty_state_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(Res.string.empty_state_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GasStationsEmptyStatePreview() {
    FuelioTheme {
        GasStationsEmptyState(modifier = Modifier.fillMaxSize())
    }
}
