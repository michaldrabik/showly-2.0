package com.michaldrabik.common.errors

sealed class ShowlyError(errorMessage: String?) : Throwable(errorMessage) {

  data class UnauthorizedError(
    val errorMessage: String?
  ) : ShowlyError(errorMessage)

  data class ValidationError(
    val errorMessage: String?
  ) : ShowlyError(errorMessage)

  data class UnknownHttpError(
    val errorMessage: String?
  ) : ShowlyError(errorMessage)

  data class UnknownError(
    val errorMessage: String?
  ) : ShowlyError(errorMessage)

  object CoroutineCancellation : ShowlyError("")
}
