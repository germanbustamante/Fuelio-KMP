package com.germandebustamante.fuelio.data.local.datastore

import com.germandebustamante.fuelio.data.di.ContextProvider

actual fun preferencesPath(contextProvider: ContextProvider): String =
    contextProvider.getAndroidContext().applicationContext.filesDir.resolve(PREFERENCES_FILE_NAME).absolutePath
