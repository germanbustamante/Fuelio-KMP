package com.germandebustamante.fuelio.designsystem.emptystate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton

@Composable
fun FuelioEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FuelioSpacing.xl, vertical = FuelioSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.height(FuelioSpacing.lg))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(FuelioSpacing.sm))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Spacer(Modifier.height(FuelioSpacing.lg))
            action()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FuelioEmptyStatePreview() {
    FuelioTheme {
        FuelioEmptyState(
            title = "No results found",
            subtitle = "Try changing your search or filters to find what you're looking for.",
            icon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
            action = { FuelioTextButton(text = "Clear filters", onClick = {}) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FuelioEmptyStateMinimalPreview() {
    FuelioTheme {
        FuelioEmptyState(title = "Nothing here yet")
    }
}
