package com.germandebustamante.fuelio.core.domain.error

fun Throwable.toDomainError(): DomainError = (this as? DomainError) ?: DomainError.Unknown
