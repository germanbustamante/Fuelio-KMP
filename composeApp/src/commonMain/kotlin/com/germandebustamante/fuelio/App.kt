package com.germandebustamante.fuelio

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.feature.list.ui.GasStationsScreen

@Composable
fun App() {
    FuelioTheme {
        GasStationsScreen(modifier = Modifier.fillMaxSize())
    }
}

@Preview
@Composable
private fun AppPreview() {
    FuelioTheme { App() }
}