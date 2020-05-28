package com.michaldrabik.showly2.ui.trakt

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.events.Event
import com.michaldrabik.showly2.common.events.TraktSyncAuthError
import com.michaldrabik.showly2.common.events.TraktSyncError
import com.michaldrabik.showly2.common.events.TraktSyncProgress
import com.michaldrabik.showly2.common.events.TraktSyncStart
import com.michaldrabik.showly2.common.events.TraktSyncSuccess
import com.michaldrabik.showly2.common.trakt.TraktSyncWorker
import com.michaldrabik.showly2.common.trakt.exports.TraktExportWatchlistRunner
import com.michaldrabik.showly2.common.trakt.imports.TraktImportWatchedRunner
import com.michaldrabik.showly2.common.trakt.imports.TraktImportWatchlistRunner
import com.michaldrabik.showly2.model.TraktSyncSchedule
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.utilities.MessageEvent.Companion.error
import com.michaldrabik.showly2.utilities.MessageEvent.Companion.info
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktSyncViewModel @Inject constructor(
  private val userManager: UserTraktManager,
  private val settingsRepository: SettingsRepository,
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
      val syncSchedule = settingsRepository.load().traktSyncSchedule
      uiState = TraktSyncUiModel(isAuthorized = isAuthorized, traktSyncSchedule = syncSchedule)
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
        _messageLiveData.value = info(R.string.textTraktLoginSuccess)
        invalidate()
      } catch (t: Throwable) {
        _messageLiveData.value = error(R.string.errorAuthorization)
      }
    }
  }

  fun saveTraktSyncSchedule(schedule: TraktSyncSchedule, context: Context) {
    viewModelScope.launch {
      val settings = settingsRepository.load()
      settings.let {
        val new = it.copy(traktSyncSchedule = schedule)
        settingsRepository.update(new)
      }
      TraktSyncWorker.schedule(schedule, context.applicationContext)
      uiState = TraktSyncUiModel(traktSyncSchedule = schedule)
    }
  }

  fun handleEvent(event: Event) {
    viewModelScope.launch {
      when (event) {
        is TraktSyncStart -> {
          _messageLiveData.value = info(R.string.textTraktSyncStarted)
          uiState = TraktSyncUiModel(isProgress = true, progressStatus = "")
        }
        is TraktSyncProgress -> {
          uiState = TraktSyncUiModel(isProgress = true, progressStatus = event.status)
        }
        is TraktSyncSuccess -> {
          uiState = TraktSyncUiModel(isProgress = false, progressStatus = "")
          _messageLiveData.value = info(R.string.textTraktSyncComplete)
        }
        is TraktSyncError -> {
          uiState = TraktSyncUiModel(isProgress = false, progressStatus = "")
          _messageLiveData.value = info(R.string.textTraktSyncError)
        }
        is TraktSyncAuthError -> {
          viewModelScope.launch {
            userManager.revokeToken()
            _messageLiveData.value = error(R.string.errorTraktAuthorization)
            uiState = TraktSyncUiModel(isProgress = false, authError = true, progressStatus = "")
          }
        }
      }
    }
  }
}
