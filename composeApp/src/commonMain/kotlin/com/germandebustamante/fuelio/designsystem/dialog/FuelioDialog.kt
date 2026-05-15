package com.germandebustamante.fuelio.designsystem.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize

@Composable
fun FuelioDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    title: String? = null,
    text: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon?.let {
            { Icon(imageVector = it, contentDescription = iconContentDescription) }
        },
        title = title?.let { { Text(it, style = MaterialTheme.typography.titleLarge) } },
        text = text?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
    )
}

@Preview(showBackground = true)
@Composable
private fun FuelioDialogPreview() {
    FuelioTheme {
        FuelioDialog(
            onDismissRequest = {},
            title = "Dialog title",
            text = "This is the dialog message with some description text.",
            confirmButton = {
                FuelioTextButton(
                    text = "Confirm",
                    onClick = {},
                    config = TextButtonConfig(size = TextButtonSize.SMALL),
                )
            },
            dismissButton = {
                FuelioTextButton(
                    text = "Cancel",
                    onClick = {},
                    config = TextButtonConfig(size = TextButtonSize.SMALL),
                )
            },
        )
    }
}
