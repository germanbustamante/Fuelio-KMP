package com.germandebustamante.fuelio.core.interop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.reflect.KClass

/**
 * Gives a Kotlin [ViewModel] an owner on iOS.
 *
 * `ViewModel.clear()` is not public API, so a `ViewModel` created straight out of Koin from Swift
 * would never have `onCleared()` called and its `viewModelScope` would keep running after the screen
 * is gone — a leak that shows up as network calls for a screen the user already left.
 *
 * The supported way to reach that from outside the lifecycle library is to own a [ViewModelStore]
 * (exactly what a `ViewModelStoreOwner` does on Android) and clear it. One store per screen, cleared
 * by [close].
 */
internal class IosViewModelHandle<T : ViewModel>(
    private val store: ViewModelStore,
    val viewModel: T,
) {
    fun close() {
        store.clear()
    }
}

/**
 * Builds a [ViewModel] through a private [ViewModelStore] so its lifetime can be ended explicitly.
 */
internal fun <T : ViewModel> createViewModelHandle(
    modelClass: KClass<T>,
    create: () -> T,
): IosViewModelHandle<T> {
    val store = ViewModelStore()
    val provider = ViewModelProvider.create(
        store = store,
        factory = viewModelFactory { addInitializer(modelClass) { create() } },
    )
    return IosViewModelHandle(store, provider[modelClass])
}
