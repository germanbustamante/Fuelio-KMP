package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.feature.common.dialog.error.ErrorDialog
import com.germandebustamante.fuelio.feature.list.state.GasStationsUIState
import com.germandebustamante.fuelio.feature.list.state.GasStationsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GasStationsScreen(
    viewModel: GasStationsViewModel = koinViewModel<GasStationsViewModel>(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GasStationsScreen(state = state, modifier = modifier)
}

@Composable
private fun GasStationsScreen(state: GasStationsUIState, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (state) {
            is GasStationsUIState.Error -> ErrorDialog(
                description = state.error.message.toString(), onDismissRequest = {},
            )

            GasStationsUIState.Loading -> CircularProgressIndicator()

            is GasStationsUIState.Success -> GasStationsContent(state)
        }
    }
}

@Composable
private fun GasStationsContent(state: GasStationsUIState.Success, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(state.gasStations, key = { it.station.id }) {
            GasStationItem(it)
        }
    }
}