package com.ll.taskflowv3.core.util

// Nuestro Wrapper. Toda función devolverá un Result.Success o un Result.Error
sealed interface Result<out D, out E: DataError> {
    data class Success<out D, out E: DataError>(val data: D): Result<D, E>
    data class Error<out D, out E: DataError>(val error: E): Result<D, E>
}