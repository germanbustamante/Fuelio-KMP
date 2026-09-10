package com.germandebustamante.fuelio.designsystem.button.config.text

data class TextButtonConfig(
    val size: TextButtonSize = TextButtonSize.LARGE,
    val state: TextButtonState = TextButtonState.IDLE,
    val variant: TextButtonVariant = TextButtonVariant.Filled,
)
