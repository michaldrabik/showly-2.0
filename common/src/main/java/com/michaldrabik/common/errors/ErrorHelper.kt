package com.michaldrabik.common.errors

import com.michaldrabik.common.errors.ShowlyError.CoroutineCancellation
import com.michaldrabik.common.errors.ShowlyError.UnauthorizedError
import com.michaldrabik.common.errors.ShowlyError.UnknownError
import com.michaldrabik.common.errors.ShowlyError.UnknownHttpError
import com.michaldrabik.common.errors.ShowlyError.ValidationError
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

object ErrorHelper {

  fun parse(error: Throwable): ShowlyError =
    when (error) {
      is CancellationException -> CoroutineCancellation
      is HttpException -> {
        when {
          error.code() in arrayOf(401, 403) -> UnauthorizedError(error.message)
          error.code() == 422 -> ValidationError(error.message)
          else -> UnknownHttpError(error.message)
        }
      }
      else -> UnknownError(error.message)
    }
}
