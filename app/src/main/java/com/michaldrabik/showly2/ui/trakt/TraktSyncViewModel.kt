package com.michaldrabik.showly2.ui.trakt

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.events.Event
import com.michaldrabik.showly2.common.events.TraktSyncAuthError
import com.michaldrabik.showly2.common.events.TraktSyncError
import com.michaldrabik.showly2.common.events.TraktSyncProgress
import com.michaldrabik.showly2.common.events.TraktSyncStart
import com.michaldrabik.showly2.common.events.TraktSyncSuccess
import com.michaldrabik.showly2.common.trakt.exports.TraktExportWatchlistRunner
import com.michaldrabik.showly2.common.trakt.imports.TraktImportWatchedRunner
import com.michaldrabik.showly2.common.trakt.imports.TraktImportWatchlistRunner
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktSyncViewModel @Inject constructor(
  private val userManager: UserTraktManager,
  importWatchedRunner: TraktImportWatchedRunner,
  importWatchlistRunner: TraktImportWatchlistRunner,
  exportWatchedRunner: TraktImportWatchedRunner,
  exportWatchlistRunner: TraktExportWatchlistRunner
) : BaseViewModel<TraktSyncUiModel>() {

  init {
    val runners = listOf(importWatchedRunner, importWatchlistRunner, exportWatchedRunner, exportWatchlistRunner)
    if (runners.any { it.isRunning }) {
      uiState = TraktSyncUiModel(isProgress = true)
    }
  }

  fun invalidate() {
    viewModelScope.launch {
      val isAuthorized = userManager.isAuthorized()
      uiState = TraktSyncUiModel(isAuthorized = isAuthorized)
    }
  }

  fun authorizeTrakt(authData: Uri?) {
    if (authData == null) return
    viewModelScope.launch {
      try {
        val code = authData.getQueryParameter("code")
        if (code.isNullOrBlank()) {
          throw IllegalStateException("Invalid Trakt authorization code.")
        }
        userManager.authorize(code)
        _messageLiveData.value = R.string.textTraktLoginSuccess
        invalidate()
      } catch (t: Throwable) {
        _errorLiveData.value = R.string.errorAuthorization
      }
    }
  }

  fun handleEvent(event: Event) {
    viewModelScope.launch {
      when (event) {
        is TraktSyncStart -> {
          _messageLiveData.value = R.string.textTraktSyncStarted
          uiState = TraktSyncUiModel(isProgress = true, progressStatus = "")
        }
        is TraktSyncProgress -> {
          uiState = TraktSyncUiModel(isProgress = true, progressStatus = event.status)
        }
        is TraktSyncSuccess -> {
          uiState = TraktSyncUiModel(isProgress = false, progressStatus = "")
          _messageLiveData.value = R.string.textTraktSyncComplete
        }
        is TraktSyncError -> {
          uiState = TraktSyncUiModel(isProgress = false, progressStatus = "")
          _messageLiveData.value = R.string.textTraktSyncError
        }
        is TraktSyncAuthError -> {
          viewModelScope.launch {
            userManager.revokeToken()
            _errorLiveData.value = R.string.errorTraktAuthorization
            uiState = TraktSyncUiModel(isProgress = false, authError = true, progressStatus = "")
          }
        }
      }
    }
  }
}
