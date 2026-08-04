package com.germandebustamante.fuelio.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.designtokens.FuelioRadiusTokens

internal val FuelioShapes = Shapes(
    extraSmall = RoundedCornerShape(FuelioRadiusTokens.EXTRA_SMALL.dp),
    small = RoundedCornerShape(FuelioRadiusTokens.SMALL.dp),
    medium = RoundedCornerShape(FuelioRadiusTokens.MEDIUM.dp),
    large = RoundedCornerShape(FuelioRadiusTokens.LARGE.dp),
    extraLarge = RoundedCornerShape(FuelioRadiusTokens.EXTRA_LARGE.dp),
)
