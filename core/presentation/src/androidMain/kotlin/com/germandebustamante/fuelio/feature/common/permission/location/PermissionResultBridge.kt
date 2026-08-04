package com.germandebustamante.fuelio.feature.common.permission.location

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object PermissionResultBridge {

    private var continuation: CancellableContinuation<Boolean>? = null

    suspend fun awaitResult(): Boolean = suspendCancellableCoroutine { cont ->
        continuation = cont
        cont.invokeOnCancellation { continuation = null }
    }

    fun deliverResult(isGranted: Boolean) {
        continuation?.resume(isGranted)
        continuation = null
    }
}
