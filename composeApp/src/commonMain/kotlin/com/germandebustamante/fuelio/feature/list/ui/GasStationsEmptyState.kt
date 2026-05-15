package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize
import com.germandebustamante.fuelio.designsystem.emptystate.FuelioEmptyState
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.empty_state_change_filters
import fuelio.composeapp.generated.resources.empty_state_subtitle
import fuelio.composeapp.generated.resources.empty_state_title
import fuelio.composeapp.generated.resources.top_bar_subtitle_change_province
import org.jetbrains.compose.resources.stringResource

@Composable
fun GasStationsEmptyState(
    onChangeProvince: (() -> Unit)? = null,
    onChangeFilters: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    FuelioEmptyState(
        title = stringResource(Res.string.empty_state_title),
        subtitle = stringResource(Res.string.empty_state_subtitle),
        icon = {
            Text(text = "⛽", fontSize = 56.sp)
        },
        action = when {
            onChangeFilters != null -> {
                {
                    FuelioTextButton(
                        text = stringResource(Res.string.empty_state_change_filters),
                        onClick = onChangeFilters,
                        config = TextButtonConfig(size = TextButtonSize.MEDIUM),
                    )
                }
            }
            onChangeProvince != null -> {
                {
                    FuelioTextButton(
                        text = stringResource(Res.string.top_bar_subtitle_change_province),
                        onClick = onChangeProvince,
                        config = TextButtonConfig(size = TextButtonSize.MEDIUM),
                    )
                }
            }
            else -> null
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun GasStationsEmptyStatePreview() {
    FuelioTheme {
        GasStationsEmptyState(
            onChangeFilters = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
