package com.germandebustamante.fuelio.designsystem.button.config.icon

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class IconButtonSize(
    val size: Dp,
    val contentPadding: Dp,
    val iconSize: Dp = 18.dp,
) {
    EXTRA_SMALL(size = 34.dp, contentPadding = 0.dp),
    SMALL(size = 40.dp, contentPadding = 10.dp),
    MEDIUM(size = 48.dp, contentPadding = 14.dp),
    LARGE(size = 56.dp, contentPadding = 14.dp, iconSize = 24.dp)
}