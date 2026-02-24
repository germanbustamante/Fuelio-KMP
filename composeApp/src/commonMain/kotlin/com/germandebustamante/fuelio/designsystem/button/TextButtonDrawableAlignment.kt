package com.germandebustamante.fuelio.designsystem.button

import org.jetbrains.compose.resources.DrawableResource

enum class TextButtonDrawableAlignment { START, END }

data class TextButtonDrawable(
    val drawableRes: DrawableResource,
    val alignment: TextButtonDrawableAlignment = TextButtonDrawableAlignment.START,
)