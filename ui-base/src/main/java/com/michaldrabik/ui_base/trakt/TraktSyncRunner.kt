package com.michaldrabik.ui_base.trakt

import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.error.TraktAuthError
import timber.log.Timber

abstract class TraktSyncRunner(
  private val userTraktManager: UserTraktManager
) {

  companion object {
    const val RETRY_DELAY_MS = 2000L
    const val MAX_RETRY_COUNT = 3
  }

  var isRunning = false
  var retryCount = 0
  var progressListener: ((String, Int, Int) -> Unit)? = null

  abstract suspend fun run(): Int

  protected suspend fun checkAuthorization() = try {
    Timber.d("Checking authorization...")
    userTraktManager.checkAuthorization()
  } catch (t: Throwable) {
    isRunning = false
    throw TraktAuthError(t.message)
  }

  protected fun resetRetries() {
    retryCount = 0
  }
}
