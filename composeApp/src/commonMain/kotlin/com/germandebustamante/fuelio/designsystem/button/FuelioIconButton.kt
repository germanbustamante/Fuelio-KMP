package com.germandebustamante.fuelio.designsystem.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonShape
import com.germandebustamante.fuelio.designsystem.button.config.icon.IconButtonSize
import com.germandebustamante.fuelio.designsystem.button.config.icon.getShape
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelioIconButton(
    onClick: () -> Unit,
    drawableRes: DrawableResource,
    modifier: Modifier = Modifier,
    config: IconButtonConfig = IconButtonConfig(),
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = config.getShape(),
        contentPadding = PaddingValues(config.size.contentPadding),
        modifier = modifier.size(config.size.size),
        enabled = enabled,
    ) {
        Icon(
            painterResource(drawableRes),
            null,
            modifier = Modifier.size(config.size.iconSize)
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
                drawableRes = Res.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.LARGE,
                    shape = IconButtonShape.CIRCLE,
                )
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = Res.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.MEDIUM,
                    shape = IconButtonShape.CIRCLE,
                )
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = Res.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.SMALL,
                    shape = IconButtonShape.CIRCLE,
                )
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = Res.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.EXTRA_SMALL,
                    shape = IconButtonShape.CIRCLE,
                )
            )

            FuelioIconButton(
                onClick = {},
                drawableRes = Res.drawable.compose_multiplatform,
                config = IconButtonConfig(
                    size = IconButtonSize.MEDIUM,
                    shape = IconButtonShape.CIRCLE,
                )
            )
        }
    }
}