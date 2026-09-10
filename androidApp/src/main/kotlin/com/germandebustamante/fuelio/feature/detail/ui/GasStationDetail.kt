@file:OptIn(ExperimentalMaterial3Api::class)

package com.germandebustamante.fuelio.feature.detail.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.core.util.formatAsEuros
import com.germandebustamante.fuelio.designsystem.card.FuelioCard
import com.germandebustamante.fuelio.designsystem.emptystate.FuelioEmptyState
import com.germandebustamante.fuelio.designsystem.progress.SkeletonBox
import com.germandebustamante.fuelio.designsystem.progress.fuelioSkeleton
import com.germandebustamante.fuelio.designsystem.scaffold.FuelioScaffold
import com.germandebustamante.fuelio.designsystem.topbar.FuelioTopBar
import com.germandebustamante.fuelio.designsystem.topbar.FuelioTopBarVariant
import com.germandebustamante.fuelio.feature.detail.state.ContentState
import com.germandebustamante.fuelio.feature.detail.state.GasStationDetailUIState
import com.germandebustamante.fuelio.feature.detail.state.GasStationDetailViewModel
import com.germandebustamante.fuelio.feature.detail.state.ScheduleDayStatus
import com.germandebustamante.fuelio.feature.detail.state.ScheduleDayVO
import com.germandebustamante.fuelio.feature.detail.state.toScheduleDays
import kotlinx.datetime.DayOfWeek
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GasStationDetail(
    route: Destination.GasStationDetails,
    modifier: Modifier = Modifier,
    viewModel: GasStationDetailViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GasStationDetail(
        state = state,
        onBackClick = viewModel::onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun GasStationDetail(state: GasStationDetailUIState, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    FuelioScaffold(
        modifier = modifier,
        topBar = {
            FuelioTopBar(
                variant = FuelioTopBarVariant.Small,
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag(A11yIdentifiers.DETAIL_BACK_BUTTON),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state.contentState,
            label = "detail_content_state",
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) { contentState ->
            when (contentState) {
                ContentState.Loading -> GasStationDetailSkeleton(modifier = Modifier.fillMaxSize())
                is ContentState.Success -> GasStationDetailContent(
                    gasStation = contentState.gasStation,
                    scheduleDays = contentState.scheduleDays,
                    modifier = Modifier.fillMaxSize(),
                )

                ContentState.NotFound -> FuelioEmptyState(
                    title = stringResource(R.string.detail_not_found_title),
                    subtitle = stringResource(R.string.detail_not_found_subtitle),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    modifier = Modifier.fillMaxSize().testTag(A11yIdentifiers.DETAIL_NOT_FOUND),
                )
            }
        }
    }
}

@Composable
private fun GasStationDetailContent(gasStation: GasStationBO, scheduleDays: List<ScheduleDayVO>, modifier: Modifier = Modifier) {
    val stationName = gasStation.displayName

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(FuelioSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FuelioSpacing.lg),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(FuelioSpacing.xxs)) {
                Text(
                    text = stationName,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.testTag(A11yIdentifiers.DETAIL_STATION_NAME),
                )
                Text(
                    text = gasStation.getFullDirection(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item { GasStationMapPreview(stationName, gasStation.latitude, gasStation.longitude) }

        item {
            GasStationDirectionsButton(
                gasStation,
                modifier = Modifier.testTag(A11yIdentifiers.DETAIL_DIRECTIONS_BUTTON),
            )
        }

        item { PricesSection(gasStation, modifier = Modifier.testTag(A11yIdentifiers.DETAIL_PRICES_SECTION)) }

        item { ScheduleSection(scheduleDays, modifier = Modifier.testTag(A11yIdentifiers.DETAIL_SCHEDULE_SECTION)) }
    }
}

private data class FuelPriceEntry(@StringRes val labelRes: Int, val price: Double?)

@Composable
private fun PricesSection(gasStation: GasStationBO, modifier: Modifier = Modifier) {
    val entries = listOf(
        FuelPriceEntry(R.string.fuel_filter_gasoline_95, gasStation.gasolinePrice95),
        FuelPriceEntry(R.string.fuel_filter_gasoline_98, gasStation.gasolinePrice98),
        FuelPriceEntry(R.string.fuel_filter_diesel, gasStation.dieselPrice),
        FuelPriceEntry(R.string.fuel_filter_diesel_premium, gasStation.dieselPremiumPrice),
    )
    val cheapestPrice = entries.mapNotNull { it.price }.minOrNull()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm)) {
        Text(
            text = stringResource(R.string.detail_prices_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        entries.chunked(2).forEach { rowEntries ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowEntries.forEach { entry ->
                    val isCheapest = entry.price != null && entry.price == cheapestPrice
                    FuelioCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(FuelioSpacing.md)) {
                            Text(
                                text = stringResource(entry.labelRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = entry.price?.formatAsEuros() ?: "—",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isCheapest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val weekDayResources: Map<DayOfWeek, Int> = mapOf(
    DayOfWeek.MONDAY to R.string.day_monday,
    DayOfWeek.TUESDAY to R.string.day_tuesday,
    DayOfWeek.WEDNESDAY to R.string.day_wednesday,
    DayOfWeek.THURSDAY to R.string.day_thursday,
    DayOfWeek.FRIDAY to R.string.day_friday,
    DayOfWeek.SATURDAY to R.string.day_saturday,
    DayOfWeek.SUNDAY to R.string.day_sunday,
)

@Composable
private fun ScheduleSection(scheduleDays: List<ScheduleDayVO>, modifier: Modifier = Modifier) {
    val todaySuffix = stringResource(R.string.detail_schedule_today_suffix)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm)) {
        Text(
            text = stringResource(R.string.detail_schedule_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FuelioCard {
            Column(modifier = Modifier.padding(horizontal = FuelioSpacing.md)) {
                scheduleDays.forEach { day ->
                    val hoursLabel = when (val status = day.status) {
                        is ScheduleDayStatus.Closed -> stringResource(R.string.detail_schedule_closed)
                        is ScheduleDayStatus.AlwaysOpen -> stringResource(R.string.detail_schedule_open_24h)
                        is ScheduleDayStatus.Hours -> "${status.start}–${status.end}"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = FuelioSpacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(weekDayResources.getValue(day.dayOfWeek)) + if (day.isToday) todaySuffix else "",
                            style = if (day.isToday) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            color = if (day.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = hoursLabel,
                            style = if (day.isToday) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            color = if (day.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            overflow = TextOverflow.Clip,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GasStationDetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(FuelioSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FuelioSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FuelioSpacing.xs)) {
            SkeletonBox(height = 24.dp, width = 200.dp)
            SkeletonBox(height = 16.dp, width = 260.dp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .fuelioSkeleton(MaterialTheme.shapes.large),
        )
        SkeletonBox(height = 44.dp, shape = MaterialTheme.shapes.extraLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm), modifier = Modifier.fillMaxWidth()) {
            SkeletonBox(height = 72.dp, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
            SkeletonBox(height = 72.dp, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
        }
        SkeletonBox(height = 220.dp, shape = MaterialTheme.shapes.large)
    }
}

@Composable
@Preview("LightMode", showBackground = true)
@Preview(name = "DarkMode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
private fun GasStationDetailContentPreview() {
    FuelioTheme {
        val gasStation = fakeGasStations.first()
        GasStationDetailContent(
            gasStation = gasStation,
            scheduleDays = gasStation.schedule.toScheduleDays(DayOfWeek.MONDAY),
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun GasStationDetailSkeletonPreview() {
    FuelioTheme {
        GasStationDetailSkeleton()
    }
}
