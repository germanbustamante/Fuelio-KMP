package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.DrawableRes
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBrand
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.core.util.formatAsEuros
import com.germandebustamante.fuelio.core.util.formatAsKilometers
import com.germandebustamante.fuelio.designsystem.card.FuelioCard
import com.germandebustamante.fuelio.feature.list.state.FuelFilter
import com.germandebustamante.fuelio.feature.list.state.GasStationItemVO
import com.germandebustamante.fuelio.feature.list.ui.components.CheapestBadge
import com.germandebustamante.fuelio.feature.list.ui.components.OpenClosedBadge

@Composable
fun GasStationItem(
    gasStation: GasStationItemVO,
    isFavorite: Boolean,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stationName = gasStation.station.displayName
    val address = gasStation.station.getFullDirection()
    val openLabel = stringResource(if (gasStation.isOpen) R.string.open else R.string.closed)
    val distanceLabel = gasStation.distanceInKilometers?.let { stringResource(R.string.at_distance, it.formatAsKilometers()) } ?: ""
    val fuelLabel = gasStation.fuelFilterLabel()
    val priceLabel = gasStation.getCurrentFuelPrice()?.formatAsEuros() ?: "N/A"
    val cheapestLabel = if (gasStation.isCheapest) ", ${stringResource(R.string.cheapest_station)}" else ""
    val a11yDesc =
        "$stationName, $address, $openLabel${if (distanceLabel.isNotEmpty()) ", $distanceLabel" else ""}, $fuelLabel a $priceLabel$cheapestLabel"

    FuelioCard(
        onClick = onItemClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = a11yDesc },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FuelioSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = priceLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (gasStation.isCheapest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "/L",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = FuelioSpacing.xxs, bottom = FuelioSpacing.xxs),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OpenClosedBadge(isOpen = gasStation.isOpen)
                    IconToggleButton(
                        checked = isFavorite,
                        onCheckedChange = { onToggleFavorite() },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = stringResource(if (isFavorite) R.string.favorite_remove else R.string.favorite_add),
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            if (gasStation.isCheapest) {
                CheapestBadge()
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GasStationBrandLogo(
                    brand = gasStation.station.brand,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(FuelioSpacing.xs)
                        .size(20.dp),
                )
                Text(
                    text = stationName,
                    style = MaterialTheme.typography.titleSmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                val distanceInKilometers = gasStation.distanceInKilometers
                if (distanceInKilometers != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.distance_km_ic),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = distanceInKilometers.formatAsKilometers(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GasStationBrandLogo(brand: GasStationBrand?, modifier: Modifier = Modifier) {
    val logoPainter = brand?.toDrawableResource()
    Icon(
        painter = painterResource(logoPainter ?: R.drawable.gas_station_ic),
        contentDescription = null,
        tint = if (logoPainter != null) Color.Unspecified else MaterialTheme.colorScheme.secondary,
        modifier = modifier,
    )
}

@DrawableRes
internal fun GasStationBrand.toDrawableResource(): Int = when (this) {
    GasStationBrand.REPSOL -> R.drawable.logo_repsol
    GasStationBrand.CEPSA -> R.drawable.logo_cepsa
    GasStationBrand.BP -> R.drawable.logo_bp
    GasStationBrand.SHELL -> R.drawable.logo_shell
    GasStationBrand.GALP -> R.drawable.logo_galp
    GasStationBrand.ALCAMPO -> R.drawable.logo_alcampo
    GasStationBrand.PETRONOR -> R.drawable.logo_petronor
    GasStationBrand.PETROPRIX -> R.drawable.logo_petroprix
    GasStationBrand.Q8 -> R.drawable.logo_q8
    GasStationBrand.BALLENOIL -> R.drawable.logo_ballenoil
    GasStationBrand.MOEVE -> R.drawable.logo_moeve
    GasStationBrand.NATURGY -> R.drawable.logo_naturgy
    GasStationBrand.CARREFOUR -> R.drawable.logo_carrefour
    GasStationBrand.COSTCO -> R.drawable.logo_costco
    GasStationBrand.PLENERGY -> R.drawable.logo_plenergy
    GasStationBrand.GACOSUR -> R.drawable.logo_gacosur
}

@Composable
private fun GasStationItemVO.fuelFilterLabel(): String = when (fuelFilter) {
    is FuelFilter.Gasoline95 -> stringResource(R.string.fuel_filter_gasoline_95)
    is FuelFilter.Gasoline98 -> stringResource(R.string.fuel_filter_gasoline_98)
    is FuelFilter.Diesel -> stringResource(R.string.fuel_filter_diesel)
    is FuelFilter.DieselPremium -> stringResource(R.string.fuel_filter_diesel_premium)
}

@Composable
@Preview("LightMode", showBackground = true)
@Preview(name = "DarkMode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
private fun GasStationItemPreview() {
    FuelioTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.md),
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            GasStationItem(
                gasStation = GasStationItemVO(fakeGasStations.first(), isCheapest = true, isOpen = true),
                isFavorite = false,
                onItemClick = {},
                onToggleFavorite = {},
            )
            GasStationItem(
                gasStation = GasStationItemVO(fakeGasStations.first(), isOpen = false),
                isFavorite = true,
                onItemClick = {},
                onToggleFavorite = {},
            )
        }
    }
}
