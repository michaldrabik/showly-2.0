package com.michaldrabik.common.errors

sealed class ShowlyError(errorMessage: String?) : Throwable(errorMessage) {

  object ValidationError : ShowlyError("ValidationError")

  object ResourceConflictError : ShowlyError("ResourceConflictError")

  object ResourceNotFoundError : ShowlyError("ResourceNotFoundError")

  object AccountLockedError : ShowlyError("AccountLockedError")

  object AccountLimitsError : ShowlyError("AccountLimitsError")

  data class UnauthorizedError(val errorMessage: String?) : ShowlyError(errorMessage)

  data class UnknownHttpError(
    val errorMessage: String?
  ) : ShowlyError(errorMessage)

  data class UnknownError(
    val errorMessage: String?
  ) : ShowlyError(errorMessage)

  object CoroutineCancellation : ShowlyError("")
}
