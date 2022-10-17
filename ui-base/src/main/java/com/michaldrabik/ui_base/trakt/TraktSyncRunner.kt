package com.michaldrabik.ui_base.trakt

import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.repository.UserTraktManager
import timber.log.Timber

abstract class TraktSyncRunner(
  private val userTraktManager: UserTraktManager,
) {

  companion object {
    const val TRAKT_LIMIT_DELAY_MS = 1100L
    const val RETRY_DELAY_MS = 2000L
    const val MAX_RETRY_COUNT = 3
  }

  var retryCount = 0
  var progressListener: (suspend (String, Int, Int) -> Unit)? = null

  abstract suspend fun run(): Int

  protected fun checkAuthorization() {
    try {
      Timber.d("Checking authorization...")
      userTraktManager.checkAuthorization()
    } catch (t: Throwable) {
      throw ShowlyError.UnauthorizedError(t.message)
    }
  }

  protected fun resetRetries() {
    retryCount = 0
  }
}
