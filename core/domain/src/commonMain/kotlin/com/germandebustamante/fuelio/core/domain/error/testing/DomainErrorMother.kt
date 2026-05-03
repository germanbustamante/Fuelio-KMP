package com.germandebustamante.fuelio.core.domain.error.testing

import com.germandebustamante.fuelio.core.domain.error.DomainError

object DomainErrorMother {
    fun networkUnavailable() = DomainError.NetworkUnavailable
    fun serverError(code: Int = 503) = DomainError.ServerError(code)
    fun unknown() = DomainError.Unknown
}
