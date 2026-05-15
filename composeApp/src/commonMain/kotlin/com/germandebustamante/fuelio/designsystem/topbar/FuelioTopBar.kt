package com.germandebustamante.fuelio.designsystem.topbar

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

enum class FuelioTopBarVariant { Small, Medium, CenterAligned }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelioTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    variant: FuelioTopBarVariant = FuelioTopBarVariant.Small,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    when (variant) {
        FuelioTopBarVariant.Small -> TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = colors ?: TopAppBarDefaults.topAppBarColors(),
            scrollBehavior = scrollBehavior,
        )
        FuelioTopBarVariant.Medium -> MediumTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = colors ?: TopAppBarDefaults.mediumTopAppBarColors(),
            scrollBehavior = scrollBehavior,
        )
        FuelioTopBarVariant.CenterAligned -> CenterAlignedTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = colors ?: TopAppBarDefaults.topAppBarColors(),
            scrollBehavior = scrollBehavior,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun FuelioTopBarSmallPreview() {
    FuelioTheme {
        FuelioTopBar(
            variant = FuelioTopBarVariant.Small,
            title = { Text("Small Top Bar") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun FuelioTopBarMediumPreview() {
    FuelioTheme {
        FuelioTopBar(
            variant = FuelioTopBarVariant.Medium,
            title = { Text("Medium Top Bar") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun FuelioTopBarCenterAlignedPreview() {
    FuelioTheme {
        FuelioTopBar(
            variant = FuelioTopBarVariant.CenterAligned,
            title = { Text("Center Aligned") },
        )
    }
}
