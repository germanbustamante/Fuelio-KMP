package com.germandebustamante.fuelio.feature.common.dialog.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.location_permission_allow
import fuelio.composeapp.generated.resources.location_permission_denied_always_description
import fuelio.composeapp.generated.resources.location_permission_denied_description
import fuelio.composeapp.generated.resources.location_permission_dismiss
import fuelio.composeapp.generated.resources.location_permission_open_settings
import fuelio.composeapp.generated.resources.location_permission_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LocationPermissionDialog(
    permissionState: LocationPermissionState,
    onPrimaryAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    val description = when (permissionState) {
        LocationPermissionState.DeniedAlways -> stringResource(Res.string.location_permission_denied_always_description)
        else -> stringResource(Res.string.location_permission_denied_description)
    }
    val primaryButtonText = when (permissionState) {
        LocationPermissionState.DeniedAlways -> stringResource(Res.string.location_permission_open_settings)
        else -> stringResource(Res.string.location_permission_allow)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.align(Alignment.Center),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.location_permission_title),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 28.dp, start = 32.dp, end = 32.dp),
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp, start = 32.dp, end = 32.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 28.dp),
                    ) {
                        FuelioTextButton(
                            text = stringResource(Res.string.location_permission_dismiss),
                            onClick = onDismiss,
                            config = TextButtonConfig(size = TextButtonSize.SMALL),
                        )
                        FuelioTextButton(
                            text = primaryButtonText,
                            onClick = onPrimaryAction,
                            config = TextButtonConfig(size = TextButtonSize.SMALL),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionDeniedDialogPreview() {
    FuelioTheme {
        LocationPermissionDialog(
            permissionState = LocationPermissionState.Denied,
            onPrimaryAction = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionDeniedAlwaysDialogPreview() {
    FuelioTheme {
        LocationPermissionDialog(
            permissionState = LocationPermissionState.DeniedAlways,
            onPrimaryAction = {},
            onDismiss = {},
        )
    }
}
