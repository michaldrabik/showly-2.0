package com.michaldrabik.common.errors

import com.michaldrabik.common.errors.ShowlyError.AccountLimitsError
import com.michaldrabik.common.errors.ShowlyError.AccountLockedError
import com.michaldrabik.common.errors.ShowlyError.CoroutineCancellation
import com.michaldrabik.common.errors.ShowlyError.ResourceConflictError
import com.michaldrabik.common.errors.ShowlyError.ResourceNotFoundError
import com.michaldrabik.common.errors.ShowlyError.UnauthorizedError
import com.michaldrabik.common.errors.ShowlyError.UnknownError
import com.michaldrabik.common.errors.ShowlyError.UnknownHttpError
import com.michaldrabik.common.errors.ShowlyError.ValidationError
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

object ErrorHelper {

  fun parse(error: Throwable): ShowlyError =
    when (error) {
      is ShowlyError -> error
      is HttpException -> {
        when (error.code()) {
          in arrayOf(401, 403) -> UnauthorizedError(error.message)
          404 -> ResourceNotFoundError
          409 -> ResourceConflictError
          420 -> AccountLimitsError
          422 -> ValidationError
          423 -> AccountLockedError
          else -> UnknownHttpError(error.message)
        }
      }
      is CancellationException -> CoroutineCancellation
      else -> UnknownError(error.message)
    }
}
