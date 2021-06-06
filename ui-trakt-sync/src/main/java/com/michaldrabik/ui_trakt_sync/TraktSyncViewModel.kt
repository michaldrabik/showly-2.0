package com.michaldrabik.ui_trakt_sync

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.TraktSyncAuthError
import com.michaldrabik.ui_base.events.TraktSyncError
import com.michaldrabik.ui_base.events.TraktSyncProgress
import com.michaldrabik.ui_base.events.TraktSyncStart
import com.michaldrabik.ui_base.events.TraktSyncSuccess
import com.michaldrabik.ui_base.trakt.TraktSyncService
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.trakt.exports.TraktExportWatchlistRunner
import com.michaldrabik.ui_base.trakt.imports.TraktImportWatchedRunner
import com.michaldrabik.ui_base.trakt.imports.TraktImportWatchlistRunner
import com.michaldrabik.ui_base.utilities.MessageEvent.Companion.error
import com.michaldrabik.ui_base.utilities.MessageEvent.Companion.info
import com.michaldrabik.ui_model.TraktSyncSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class TraktSyncViewModel @Inject constructor(
  @Named("miscPreferences") private var miscPreferences: SharedPreferences,
  private val userManager: UserTraktManager,
  private val settingsRepository: SettingsRepository,
  private val dateFormatProvider: DateFormatProvider,
  importWatchedRunner: TraktImportWatchedRunner,
  importWatchlistRunner: TraktImportWatchlistRunner,
  exportWatchedRunner: TraktImportWatchedRunner,
  exportWatchlistRunner: TraktExportWatchlistRunner
) : BaseViewModel<TraktSyncUiModel>() {

  init {
    val runners = listOf(
      importWatchedRunner,
      importWatchlistRunner,
      exportWatchedRunner,
      exportWatchlistRunner
    )
    if (runners.any { it.isRunning }) {
      uiState = TraktSyncUiModel(isProgress = true)
    }
  }

  fun invalidate() {
    viewModelScope.launch {
      val isAuthorized = userManager.isAuthorized()
      val settings = settingsRepository.load()
      val timestamp = miscPreferences.getLong(TraktSyncService.KEY_LAST_SYNC_TIMESTAMP, 0)

      uiState = TraktSyncUiModel(
        isAuthorized = isAuthorized,
        traktSyncSchedule = settings.traktSyncSchedule,
        quickSyncEnabled = settings.traktQuickSyncEnabled,
        lastTraktSyncTimestamp = timestamp,
        dateFormat = dateFormatProvider.loadFullHourFormat()
      )
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
      } catch (error: Throwable) {
        val message = when {
          error is HttpException && error.code() == 423 -> R.string.errorTraktLocked
          else -> R.string.errorAuthorization
        }
        _messageLiveData.value = error(message)
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
