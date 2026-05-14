package com.germandebustamante.fuelio.feature.list.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.close_ic
import fuelio.composeapp.generated.resources.search_gas_station
import fuelio.composeapp.generated.resources.search_ic
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

//region Public API

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GasStationSearchBar(
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val hasText by remember { derivedStateOf { textFieldState.text.isNotEmpty() } }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { onQueryChange(it) }
    }

    SearchBar(
        state = searchBarState,
        inputField = {
            SearchInputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                hasText = hasText,
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

//endregion

//region Private components

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInputField(
    textFieldState: TextFieldState,
    searchBarState: SearchBarState,
    hasText: Boolean,
) {
    SearchBarDefaults.InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        onSearch = {},
        placeholder = { Text(stringResource(Res.string.search_gas_station)) },
        leadingIcon = { SearchIcon() },
        trailingIcon = if (hasText) {
            { ClearIcon(onClick = { textFieldState.clearText() }) }
        } else null,
    )
}

@Composable
private fun SearchIcon() {
    Icon(
        painter = painterResource(Res.drawable.search_ic),
        contentDescription = null,
    )
}

@Composable
private fun ClearIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(Res.drawable.close_ic),
            contentDescription = null,
        )
    }
}

//endregion

//region Preview

@Preview(showBackground = true)
@Composable
private fun GasStationSearchBarPreview() {
    FuelioTheme {
        GasStationSearchBar(onQueryChange = {})
    }
}

//endregion
