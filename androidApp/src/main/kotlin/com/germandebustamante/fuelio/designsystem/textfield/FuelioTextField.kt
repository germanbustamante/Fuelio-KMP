package com.germandebustamante.fuelio.designsystem.textfield

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

@Composable
fun FuelioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    helperText: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
        )
        if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = FuelioSpacing.md, top = FuelioSpacing.xs),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FuelioTextFieldPreview() {
    FuelioTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.md),
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            FuelioTextField(value = "", onValueChange = {}, label = "Label", placeholder = "Placeholder")
            FuelioTextField(value = "With value", onValueChange = {}, label = "Label")
            FuelioTextField(
                value = "Error state",
                onValueChange = {},
                label = "Label",
                isError = true,
                helperText = "This field has an error",
            )
            FuelioTextField(
                value = "",
                onValueChange = {},
                label = "With helper",
                helperText = "Helpful hint text",
            )
            FuelioTextField(value = "Disabled", onValueChange = {}, label = "Label", enabled = false)
        }
    }
}
