package com.michaldrabik.ui_base.trakt

import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.repository.UserTraktManager
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

abstract class TraktSyncRunner(
  private val userTraktManager: UserTraktManager,
) {

  companion object {
    const val TRAKT_LIMIT_DELAY_MS = 1250L
    const val RETRY_DELAY_MS = 5000L
    const val MAX_EXPORT_RETRY_COUNT = 1
    const val MAX_IMPORT_RETRY_COUNT = 3
  }

  val retryCount = AtomicInteger(0)
  var progressListener: (suspend (String, Int, Int) -> Unit)? = null

  abstract suspend fun run(): Int

  protected fun checkAuthorization() {
    try {
      Timber.d("Checking authorization...")
      userTraktManager.checkAuthorization()
    } catch (error: Throwable) {
      throw ShowlyError.UnauthorizedError(error.message)
    }
  }

  protected fun resetRetries() {
    retryCount.set(0)
  }
}
