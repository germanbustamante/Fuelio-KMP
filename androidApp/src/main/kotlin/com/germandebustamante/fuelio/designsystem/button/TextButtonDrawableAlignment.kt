package com.germandebustamante.fuelio.designsystem.button

import androidx.annotation.DrawableRes

enum class TextButtonDrawableAlignment { START, END }

data class TextButtonDrawable(
    @DrawableRes val drawableRes: Int,
    val alignment: TextButtonDrawableAlignment = TextButtonDrawableAlignment.START,
)
