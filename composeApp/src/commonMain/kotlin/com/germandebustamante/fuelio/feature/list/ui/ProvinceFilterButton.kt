package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.province_filter_ic
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProvinceFilterButton(province: ProvinceBO, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AssistChip(
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.province_filter_ic),
                contentDescription = null,
            )
        },
        label = { Text(text = province.name) },
        shape = ShapeDefaults.Medium,
        onClick = onClick,
        modifier = modifier
    )
}

@Preview
@Composable
fun ItemProvinceButtonPreview() {
    FuelioTheme {
        ProvinceFilterButton(province = ProvinceBO("3", "Sevilla"), onClick = {})
    }
}