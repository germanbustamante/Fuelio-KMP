package com.germandebustamante.fuelio.designsystem.button.config.text

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class TextButtonSize(
    val minWidth: Dp,
    val minHeight: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
) {
    SMALL(minWidth = 62.dp, minHeight = 36.dp, horizontalPadding = 16.dp, verticalPadding = 8.dp),
    MEDIUM(minWidth = 120.dp, minHeight = 40.dp, horizontalPadding = 12.dp, verticalPadding = 10.dp),
    LARGE(minWidth = 180.dp, minHeight = 44.dp, horizontalPadding = 32.dp, verticalPadding = 12.dp),
}
