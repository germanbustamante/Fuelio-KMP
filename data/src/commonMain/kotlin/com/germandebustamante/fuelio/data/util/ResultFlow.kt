package com.germandebustamante.fuelio.data.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> resultFlow(block: suspend () -> T): Flow<Result<T>> = flow {
    try {
        emit(Result.success(block()))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        emit(Result.failure(e))
    }
}
