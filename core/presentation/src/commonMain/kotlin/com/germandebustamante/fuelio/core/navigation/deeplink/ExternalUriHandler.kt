package com.germandebustamante.fuelio.core.navigation.deeplink

object ExternalUriHandler {

    private var pendingUri: String? = null

    var listener: ((String) -> Unit)? = null
        set(value) {
            field = value
            pendingUri?.let { uri ->
                pendingUri = null
                value?.invoke(uri)
            }
        }

    fun onNewUri(uri: String) {
        val currentListener = listener
        if (currentListener != null) {
            currentListener(uri)
        } else {
            pendingUri = uri
        }
    }
}
