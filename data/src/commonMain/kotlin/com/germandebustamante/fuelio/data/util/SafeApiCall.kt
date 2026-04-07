package com.germandebustamante.fuelio.data.util

import com.germandebustamante.fuelio.core.domain.error.DomainError
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        throw when (e) {
            is DomainError -> e
            is IOException -> DomainError.NetworkUnavailable
            else -> DomainError.Unknown
        }
    }
