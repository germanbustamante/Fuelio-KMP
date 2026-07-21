package com.germandebustamante.fuelio.feature.detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.detail.state.GasStationDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GasStationDetail(
    route: Destination.GasStationDetails,
    modifier: Modifier = Modifier,
    viewModel: GasStationDetailViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {



}