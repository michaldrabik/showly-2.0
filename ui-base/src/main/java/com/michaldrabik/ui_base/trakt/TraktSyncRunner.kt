package com.michaldrabik.ui_base.trakt

import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.error.TraktAuthError
import com.michaldrabik.ui_repository.UserTraktManager

abstract class TraktSyncRunner(
  private val userTraktManager: UserTraktManager
) {

  companion object {
    const val RETRY_DELAY_MS = 5000L
  }

  var isRunning = false
  var progressListener: ((Show, Int, Int) -> Unit)? = null

  abstract suspend fun run(): Int

  protected suspend fun checkAuthorization() = try {
    userTraktManager.checkAuthorization()
  } catch (t: Throwable) {
    isRunning = false
    throw TraktAuthError(t.message)
  }
}
