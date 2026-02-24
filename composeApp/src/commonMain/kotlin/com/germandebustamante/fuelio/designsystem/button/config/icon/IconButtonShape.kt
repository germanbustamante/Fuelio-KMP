package com.germandebustamante.fuelio.designsystem.button.config.icon

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

enum class IconButtonShape { CIRCLE, SQUARE }

fun IconButtonConfig.getShape() = when (shape) {
    IconButtonShape.CIRCLE -> CircleShape
    IconButtonShape.SQUARE -> RoundedCornerShape(6.dp)
}
