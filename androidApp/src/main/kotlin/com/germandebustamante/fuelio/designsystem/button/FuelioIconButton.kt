package com.germandebustamante.fuelio.designsystem.button

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonShape
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonSize
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonVariant
import com.germandebustamante.fuelio.designsystem.button.config.icon.getShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelioIconButton(
    onClick: () -> Unit,
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier,
    config: IconButtonConfig = IconButtonConfig(),
    enabled: Boolean = true,
    @StringRes contentDescriptionRes: Int? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = config.getShape()
    val contentPadding = PaddingValues(config.size.contentPadding)
    val buttonModifier = modifier.size(config.size.size)
    val contentDescription = contentDescriptionRes?.let { stringResource(it) }
    val iconContent: @Composable () -> Unit = {
        Icon(
            painterResource(drawableRes),
            contentDescription,
            modifier = Modifier.size(config.size.iconSize),
        )
    }

    when (config.variant) {
        IconButtonVariant.Standard -> Button(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            modifier = buttonModifier,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(),
            content = { iconContent() },
        )
        IconButtonVariant.Filled -> Button(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            modifier = buttonModifier,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(),
            content = { iconContent() },
        )
        IconButtonVariant.FilledTonal -> Button(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            modifier = buttonModifier,
            enabled = enabled,
            colors = ButtonDefaults.filledTonalButtonColors(),
            content = { iconContent() },
        )
        IconButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            modifier = buttonModifier,
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(),
            content = { iconContent() },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun FuelioIconButtonPreview() {
    FuelioTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FuelioIconButton(
                onClick = {},
                drawableRes = R.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.LARGE,
                    shape = IconButtonShape.CIRCLE,
                    variant = IconButtonVariant.Standard,
                ),
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = R.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.LARGE,
                    shape = IconButtonShape.CIRCLE,
                    variant = IconButtonVariant.Filled,
                ),
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = R.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.MEDIUM,
                    shape = IconButtonShape.CIRCLE,
                    variant = IconButtonVariant.FilledTonal,
                ),
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = R.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.MEDIUM,
                    shape = IconButtonShape.CIRCLE,
                    variant = IconButtonVariant.Outlined,
                ),
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = R.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.SMALL,
                    shape = IconButtonShape.CIRCLE,
                ),
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = R.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.EXTRA_SMALL,
                    shape = IconButtonShape.CIRCLE,
                ),
            )
        }
    }
}
