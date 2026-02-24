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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonState
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

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(
            horizontal = config.size.horizontalPadding,
            vertical = config.size.verticalPadding
        ),
        enabled = isEnabled && config.state != TextButtonState.LOADING,
        modifier = modifier.defaultMinSize(minWidth = config.size.minWidth, minHeight = config.size.minHeight)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
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
}

@Composable
private fun ButtonContent(
    text: String,
    drawable: TextButtonDrawable?,
    config: TextButtonConfig,
    isPressed: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.background(Color.White)
        ) {
            Text("Enabled to test with loading")

            FuelioTextButton(
                onClick = {
                    state = if (state == TextButtonState.IDLE) {
                        TextButtonState.LOADING
                    } else {
                        TextButtonState.IDLE
                    }
                },
                text = "Large Primary Filled",
                config = TextButtonConfig(
                    size = TextButtonSize.LARGE,
                    state = state
                )
            )

            FuelioTextButton(
                onClick = {},
                text = "Medium Primary outlined",
                config = TextButtonConfig(
                    size = TextButtonSize.MEDIUM,
                    state = state
                )
            )

            FuelioTextButton(
                onClick = {},
                text = "Small Secondary filled",
                config = TextButtonConfig(
                    size = TextButtonSize.SMALL,
                    state = state
                )
            )

            FuelioTextButton(
                onClick = {},
                text = "Large Secondary outlined",
                config = TextButtonConfig(
                    size = TextButtonSize.LARGE,
                    state = state
                )
            )

            FuelioTextButton(
                onClick = {},
                text = "Medium Ghost",
                config = TextButtonConfig(
                    size = TextButtonSize.MEDIUM,
                    state = state
                )
            )

            FuelioTextButton(
                text = "Boton que tambien cambia estado (Siembre habilitado)",
                onClick = {
                    state = if (state == TextButtonState.IDLE) {
                        TextButtonState.LOADING
                    } else {
                        TextButtonState.IDLE
                    }
                }
            )

            Text("Alway disabled")

            FuelioTextButton(
                onClick = {},
                text = "Disabled Primary Filled",
                isEnabled = false,
                config = TextButtonConfig()
            )

            FuelioTextButton(
                onClick = {},
                text = "Disabled Primary Outlined",
                isEnabled = false,
                config = TextButtonConfig()
            )

            FuelioTextButton(
                onClick = {},
                text = "Disabled Secondary Filled",
                isEnabled = false,
                config = TextButtonConfig()
            )

            FuelioTextButton(
                onClick = {},
                text = "Disabled Secondary Outlined",
                isEnabled = false,
                config = TextButtonConfig()
            )

            FuelioTextButton(
                onClick = {},
                text = "Disabled Ghost",
                isEnabled = false,
                config = TextButtonConfig()
            )
        }
    }
}