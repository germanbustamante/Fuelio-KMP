package com.germandebustamante.fuelio.feature.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun ViewModel.launchStartupTasks(vararg tasks: suspend CoroutineScope.() -> Unit) {
    tasks.forEach { task -> viewModelScope.launch(block = task) }
}
