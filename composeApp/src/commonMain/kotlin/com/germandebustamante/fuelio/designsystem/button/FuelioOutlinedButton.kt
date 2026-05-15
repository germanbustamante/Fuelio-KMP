package com.germandebustamante.fuelio.designsystem.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonState
import org.jetbrains.compose.resources.painterResource

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
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(
            horizontal = config.size.horizontalPadding,
            vertical = config.size.verticalPadding,
        ),
        enabled = isEnabled && !isLoading,
        modifier = modifier.defaultMinSize(minWidth = config.size.minWidth, minHeight = config.size.minHeight),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            if (drawable?.alignment == TextButtonDrawableAlignment.START) {
                Icon(painter = painterResource(drawable.drawableRes), contentDescription = null)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (drawable?.alignment == TextButtonDrawableAlignment.END) {
                Icon(painter = painterResource(drawable.drawableRes), contentDescription = null)
            }
        }
    }
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
