package com.germandebustamante.fuelio.designsystem.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioElevation
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

enum class FuelioCardVariant { Filled, Elevated, Outlined }

@Composable
fun FuelioCard(
    modifier: Modifier = Modifier,
    variant: FuelioCardVariant = FuelioCardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    when (variant) {
        FuelioCardVariant.Filled -> {
            if (onClick != null) {
                Card(
                    onClick = onClick,
                    modifier = modifier,
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = FuelioElevation.level1),
                    content = content,
                )
            } else {
                Card(
                    modifier = modifier,
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = FuelioElevation.level1),
                    content = content,
                )
            }
        }
        FuelioCardVariant.Elevated -> {
            if (onClick != null) {
                ElevatedCard(
                    onClick = onClick,
                    modifier = modifier,
                    shape = shape,
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = FuelioElevation.level2),
                    content = content,
                )
            } else {
                ElevatedCard(
                    modifier = modifier,
                    shape = shape,
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = FuelioElevation.level2),
                    content = content,
                )
            }
        }
        FuelioCardVariant.Outlined -> {
            if (onClick != null) {
                OutlinedCard(
                    onClick = onClick,
                    modifier = modifier,
                    shape = shape,
                    content = content,
                )
            } else {
                OutlinedCard(
                    modifier = modifier,
                    shape = shape,
                    content = content,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FuelioCardPreview() {
    FuelioTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.md),
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            FuelioCard(variant = FuelioCardVariant.Filled) {
                Text("Filled card", modifier = Modifier.padding(FuelioSpacing.md))
            }
            FuelioCard(variant = FuelioCardVariant.Elevated) {
                Text("Elevated card", modifier = Modifier.padding(FuelioSpacing.md))
            }
            FuelioCard(variant = FuelioCardVariant.Outlined) {
                Text("Outlined card", modifier = Modifier.padding(FuelioSpacing.md))
            }
            FuelioCard(variant = FuelioCardVariant.Filled, onClick = {}) {
                Text("Clickable filled card", modifier = Modifier.padding(FuelioSpacing.md))
            }
        }
    }
}
