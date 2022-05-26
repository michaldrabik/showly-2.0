package com.michaldrabik.common.errors

import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

object ErrorHelper {

  fun parse(error: Throwable): ShowlyError =
    when (error) {
      is CancellationException -> ShowlyError.CoroutineCancellation
      is HttpException -> {
        when {
          error.code() in arrayOf(401, 403) -> ShowlyError.UnauthorizedError(error.message)
          else -> ShowlyError.UnknownHttpError(error.message)
        }
      }
      else -> ShowlyError.UnknownError(error.message)
    }
}
