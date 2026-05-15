package com.germandebustamante.fuelio.designsystem.button

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonState
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonVariant
import org.jetbrains.compose.resources.painterResource

@Composable
fun FuelioTextButton(
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
        config = config.copy(state = isLoading.toTextButtonState()),
        isEnabled = isEnabled,
        drawable = drawable
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelioTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    config: TextButtonConfig = TextButtonConfig(),
    isEnabled: Boolean = true,
    drawable: TextButtonDrawable? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(50)
    val contentPadding = PaddingValues(
        horizontal = config.size.horizontalPadding,
        vertical = config.size.verticalPadding
    )
    val buttonEnabled = isEnabled && config.state != TextButtonState.LOADING
    val buttonModifier = modifier.defaultMinSize(minWidth = config.size.minWidth, minHeight = config.size.minHeight)
    val content: @Composable () -> Unit = {
        Box(contentAlignment = Alignment.Center) {
            ButtonContent(
                text = text,
                drawable = drawable,
                config = config,
                isPressed = isPressed
            )

            if (config.state == TextButtonState.LOADING) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    when (config.variant) {
        TextButtonVariant.Filled -> Button(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            enabled = buttonEnabled,
            modifier = buttonModifier,
            content = { content() },
        )
        TextButtonVariant.FilledTonal -> FilledTonalButton(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            enabled = buttonEnabled,
            modifier = buttonModifier,
            content = { content() },
        )
        TextButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            enabled = buttonEnabled,
            modifier = buttonModifier,
            content = { content() },
        )
        TextButtonVariant.Text -> TextButton(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            enabled = buttonEnabled,
            modifier = buttonModifier,
            content = { content() },
        )
        TextButtonVariant.Elevated -> ElevatedButton(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            enabled = buttonEnabled,
            modifier = buttonModifier,
            content = { content() },
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    drawable: TextButtonDrawable?,
    config: TextButtonConfig,
    isPressed: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.alpha(
            if (config.state == TextButtonState.LOADING) 0f else 1f
        )
    ) {
        if (drawable?.alignment == TextButtonDrawableAlignment.START) {
            Icon(
                painter = painterResource(drawable.drawableRes),
                contentDescription = null,
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        if (drawable?.alignment == TextButtonDrawableAlignment.END) {
            Icon(
                painter = painterResource(drawable.drawableRes),
                contentDescription = null,
            )
        }
    }
}

private fun Boolean.toTextButtonState() = if (this) TextButtonState.LOADING else TextButtonState.IDLE

@Composable
@Preview
private fun ButtonPreview() {
    var state by rememberSaveable { mutableStateOf(TextButtonState.IDLE) }
    FuelioTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.xs),
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            Text("All 5 MDC3 variants")

            FuelioTextButton(
                onClick = {
                    state = if (state == TextButtonState.IDLE) {
                        TextButtonState.LOADING
                    } else {
                        TextButtonState.IDLE
                    }
                },
                text = "Filled (tap to toggle loading)",
                config = TextButtonConfig(
                    size = TextButtonSize.LARGE,
                    state = state,
                    variant = TextButtonVariant.Filled,
                )
            )

            FuelioTextButton(
                onClick = {},
                text = "FilledTonal",
                config = TextButtonConfig(
                    size = TextButtonSize.LARGE,
                    variant = TextButtonVariant.FilledTonal,
                )
            )

            FuelioTextButton(
                onClick = {},
                text = "Outlined",
                config = TextButtonConfig(
                    size = TextButtonSize.LARGE,
                    variant = TextButtonVariant.Outlined,
                )
            )

            FuelioTextButton(
                onClick = {},
                text = "Text",
                config = TextButtonConfig(
                    size = TextButtonSize.LARGE,
                    variant = TextButtonVariant.Text,
                )
            )

            FuelioTextButton(
                onClick = {},
                text = "Elevated",
                config = TextButtonConfig(
                    size = TextButtonSize.LARGE,
                    variant = TextButtonVariant.Elevated,
                )
            )

            Text("Disabled states")

            FuelioTextButton(
                onClick = {},
                text = "Filled disabled",
                isEnabled = false,
                config = TextButtonConfig(variant = TextButtonVariant.Filled)
            )

            FuelioTextButton(
                onClick = {},
                text = "Outlined disabled",
                isEnabled = false,
                config = TextButtonConfig(variant = TextButtonVariant.Outlined)
            )
        }
    }
}
