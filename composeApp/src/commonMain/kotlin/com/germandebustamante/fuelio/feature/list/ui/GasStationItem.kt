package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.at_distance
import fuelio.composeapp.generated.resources.cheapest_station
import fuelio.composeapp.generated.resources.closed
import fuelio.composeapp.generated.resources.distance_km_ic
import fuelio.composeapp.generated.resources.favorite_add
import fuelio.composeapp.generated.resources.favorite_remove
import fuelio.composeapp.generated.resources.fuel_filter_diesel
import fuelio.composeapp.generated.resources.fuel_filter_diesel_premium
import fuelio.composeapp.generated.resources.fuel_filter_gasoline_95
import fuelio.composeapp.generated.resources.fuel_filter_gasoline_98
import fuelio.composeapp.generated.resources.gas_station_ic
import fuelio.composeapp.generated.resources.logo_alcampo
import fuelio.composeapp.generated.resources.logo_ballenoil
import fuelio.composeapp.generated.resources.logo_bp
import fuelio.composeapp.generated.resources.logo_carrefour
import fuelio.composeapp.generated.resources.logo_cepsa
import fuelio.composeapp.generated.resources.logo_costco
import fuelio.composeapp.generated.resources.logo_gacosur
import fuelio.composeapp.generated.resources.logo_galp
import fuelio.composeapp.generated.resources.logo_moeve
import fuelio.composeapp.generated.resources.logo_naturgy
import fuelio.composeapp.generated.resources.logo_petronor
import fuelio.composeapp.generated.resources.logo_petroprix
import fuelio.composeapp.generated.resources.logo_plenergy
import fuelio.composeapp.generated.resources.logo_q8
import fuelio.composeapp.generated.resources.logo_repsol
import fuelio.composeapp.generated.resources.logo_shell
import fuelio.composeapp.generated.resources.open
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GasStationItem(
    gasStation: GasStationItemVO,
    isFavorite: Boolean,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stationName = gasStation.station.name.lowercase().capitalize(Locale.current)
    val address = gasStation.station.getFullDirection()
    val openLabel = stringResource(if (gasStation.isOpen) Res.string.open else Res.string.closed)
    val distanceLabel = gasStation.distanceInKilometers?.let { stringResource(Res.string.at_distance, it.formatAsKilometers()) } ?: ""
    val fuelLabel = gasStation.fuelFilterLabel()
    val priceLabel = gasStation.getCurrentFuelPrice()?.formatAsEuros() ?: "N/A"
    val cheapestLabel = if (gasStation.isCheapest) ", ${stringResource(Res.string.cheapest_station)}" else ""
    val a11yDesc =
        "$stationName, $address, $openLabel${if (distanceLabel.isNotEmpty()) ", $distanceLabel" else ""}, $fuelLabel a $priceLabel$cheapestLabel"

    FuelioCard(
        onClick = onItemClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = a11yDesc },
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FuelioSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GasStationBrandLogo(
                    brand = gasStation.station.brand,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.small)
                        .padding(FuelioSpacing.sm)
                        .size(20.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(FuelioSpacing.xxs),
                ) {
                    Text(
                        text = stationName,
                        style = MaterialTheme.typography.bodyLarge,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OpenClosedBadge(isOpen = gasStation.isOpen)
                        if (gasStation.distanceInKilometers != null) {
                            Icon(
                                painter = painterResource(Res.drawable.distance_km_ic),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = gasStation.distanceInKilometers.formatAsKilometers(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = priceLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = fuelLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconToggleButton(
                        checked = isFavorite,
                        onCheckedChange = { onToggleFavorite() },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = stringResource(if (isFavorite) Res.string.favorite_remove else Res.string.favorite_add),
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            if (gasStation.isCheapest) {
                CheapestBadge(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = FuelioSpacing.xs, end = FuelioSpacing.xs)
                )
            }
        }
    }
}

@Composable
private fun GasStationBrandLogo(brand: GasStationBrand?, modifier: Modifier = Modifier) {
    val logoPainter = brand?.toDrawableResource()
    Icon(
        painter = painterResource(logoPainter ?: Res.drawable.gas_station_ic),
        contentDescription = null,
        tint = if (logoPainter != null) Color.Unspecified else MaterialTheme.colorScheme.secondary,
        modifier = modifier,
    )
}

internal fun GasStationBrand.toDrawableResource(): DrawableResource = when (this) {
    GasStationBrand.REPSOL -> Res.drawable.logo_repsol
    GasStationBrand.CEPSA -> Res.drawable.logo_cepsa
    GasStationBrand.BP -> Res.drawable.logo_bp
    GasStationBrand.SHELL -> Res.drawable.logo_shell
    GasStationBrand.GALP -> Res.drawable.logo_galp
    GasStationBrand.ALCAMPO -> Res.drawable.logo_alcampo
    GasStationBrand.PETRONOR -> Res.drawable.logo_petronor
    GasStationBrand.PETROPRIX -> Res.drawable.logo_petroprix
    GasStationBrand.Q8 -> Res.drawable.logo_q8
    GasStationBrand.BALLENOIL -> Res.drawable.logo_ballenoil
    GasStationBrand.MOEVE -> Res.drawable.logo_moeve
    GasStationBrand.NATURGY -> Res.drawable.logo_naturgy
    GasStationBrand.CARREFOUR -> Res.drawable.logo_carrefour
    GasStationBrand.COSTCO -> Res.drawable.logo_costco
    GasStationBrand.PLENERGY -> Res.drawable.logo_plenergy
    GasStationBrand.GACOSUR -> Res.drawable.logo_gacosur
}

@Composable
private fun GasStationItemVO.fuelFilterLabel(): String = when (fuelFilter) {
    is FuelFilter.Gasoline95 -> stringResource(Res.string.fuel_filter_gasoline_95)
    is FuelFilter.Gasoline98 -> stringResource(Res.string.fuel_filter_gasoline_98)
    is FuelFilter.Diesel -> stringResource(Res.string.fuel_filter_diesel)
    is FuelFilter.DieselPremium -> stringResource(Res.string.fuel_filter_diesel_premium)
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
