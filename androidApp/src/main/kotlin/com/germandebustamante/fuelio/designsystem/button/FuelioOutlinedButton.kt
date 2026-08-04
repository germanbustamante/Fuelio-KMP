package com.germandebustamante.fuelio.designsystem.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonVariant

@Composable
fun FuelioOutlinedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    config: TextButtonConfig = TextButtonConfig(),
    isEnabled: Boolean = true,
    drawable: TextButtonDrawable? = null,
) {
    FuelioTextButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        isLoading = isLoading,
        config = config.copy(variant = TextButtonVariant.Outlined),
        isEnabled = isEnabled,
        drawable = drawable,
    )
}

@Preview(showBackground = true)
@Composable
private fun FuelioOutlinedButtonPreview() {
    FuelioTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.xs),
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            FuelioOutlinedButton(onClick = {}, text = "Large", config = TextButtonConfig(size = TextButtonSize.LARGE))
            FuelioOutlinedButton(onClick = {}, text = "Medium", config = TextButtonConfig(size = TextButtonSize.MEDIUM))
            FuelioOutlinedButton(onClick = {}, text = "Small", config = TextButtonConfig(size = TextButtonSize.SMALL))
            FuelioOutlinedButton(onClick = {}, text = "Loading", isLoading = true)
            FuelioOutlinedButton(onClick = {}, text = "Disabled", isEnabled = false)
        }
    }
}
