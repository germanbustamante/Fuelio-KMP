package com.germandebustamante.fuelio

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.feature.list.ui.GasStationsScreen

@Composable
fun App() {
    FuelioTheme {
        GasStationsScreen(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun AppPreview() {
    FuelioTheme { App() }
}