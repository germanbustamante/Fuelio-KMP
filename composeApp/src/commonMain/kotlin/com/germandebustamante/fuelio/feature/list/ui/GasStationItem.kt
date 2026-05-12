package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.core.util.formatAsEuros
import com.germandebustamante.fuelio.core.util.formatAsKilometers
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.feature.list.state.GasStationItemVO
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.closed
import fuelio.composeapp.generated.resources.distance_km_ic
import fuelio.composeapp.generated.resources.gas_station_ic
import fuelio.composeapp.generated.resources.open
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GasStationItem(gasStation: GasStationItemVO, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = CardDefaults.elevatedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.gas_station_ic),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.small)
                    .padding(8.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = gasStation.station.name.lowercase().capitalize(Locale.current),
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Text(
                    text = gasStation.station.getFullDirection(),
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                ScheduleAndLocationChip(
                    isOpened = gasStation.isOpen,
                    kilometersDistance = gasStation.distanceInKilometers
                )
            }

            Text(
                text = gasStation.getCurrentFuelPrice()?.formatAsEuros() ?: "N/A",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun ScheduleAndLocationChip(
    isOpened: Boolean,
    kilometersDistance: Double?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val (color, stringRes) = if (isOpened) {
            Pair(MaterialTheme.colorScheme.tertiary, Res.string.open)
        } else {
            Pair(MaterialTheme.colorScheme.error, Res.string.closed)
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            text = stringResource(stringRes),
            style = MaterialTheme.typography.bodySmall
        )

        if (kilometersDistance != null) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )

            Icon(
                painter = painterResource(Res.drawable.distance_km_ic),
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = kilometersDistance.formatAsKilometers(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
@Preview("LightMode", showBackground = true, showSystemUi = true)
@Preview(
    name = "DarkMode",
    showBackground = true,
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES,
    showSystemUi = true
)
private fun GasStationItemPreview() {
    FuelioTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(20.dp)) {
            GasStationItem(GasStationItemVO(fakeGasStations.first()),)

            GasStationItem(GasStationItemVO(fakeGasStations.first()))
        }

    }
}