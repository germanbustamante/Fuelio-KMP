package com.germandebustamante.fuelio.feature.common.viewmodel

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineScope

fun ViewModel.launchStartupTasks(vararg tasks: suspend CoroutineScope.() -> Unit) {
    tasks.forEach { task -> viewModelScope.launch(block = task) }
}
