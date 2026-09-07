package com.germandebustamante.fuelio.feature.list.ui.state

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import com.germandebustamante.fuelio.designsystem.emptystate.FuelioEmptyState

@Composable
fun GasStationsErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FuelioEmptyState(
        title = stringResource(R.string.error_state_title),
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
                text = stringResource(R.string.error_state_retry),
                onClick = onRetry,
                modifier = Modifier.testTag(A11yIdentifiers.RETRY_BUTTON),
            )
        },
        modifier = modifier.fillMaxSize().testTag(A11yIdentifiers.ERROR_STATE),
    )
}
