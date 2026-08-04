package com.germandebustamante.fuelio.designsystem.searchbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

@Composable
fun FuelioSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    clearContentDescription: String = "",
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = clearContentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        enabled = enabled,
        visualTransformation = VisualTransformation.None,
    )
}

@Preview(showBackground = true)
@Composable
private fun FuelioSearchBarPreview() {
    FuelioTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.md),
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            FuelioSearchBar(query = "", onQueryChange = {}, placeholder = "Search...")
            FuelioSearchBar(query = "Gas station", onQueryChange = {}, placeholder = "Search...")
            FuelioSearchBar(query = "", onQueryChange = {}, placeholder = "Disabled", enabled = false)
        }
    }
}
