package com.germandebustamante.fuelio.core.domain.error

sealed class DomainError(message: String? = null) : Throwable(message) {
    data object NetworkUnavailable : DomainError("Network unavailable")
    data class ServerError(val code: Int) : DomainError("Server error: $code")
    data object Unknown : DomainError("Unknown error")
}
