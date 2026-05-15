package com.germandebustamante.fuelio.feature.list.ui.state

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import com.germandebustamante.fuelio.designsystem.emptystate.FuelioEmptyState
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.error_state_retry
import fuelio.composeapp.generated.resources.error_state_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GasStationsErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FuelioEmptyState(
        title = stringResource(Res.string.error_state_title),
        subtitle = message.ifBlank { null },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        action = {
            FuelioTextButton(
                text = stringResource(Res.string.error_state_retry),
                onClick = onRetry,
            )
        },
        modifier = modifier.fillMaxSize(),
    )
}
