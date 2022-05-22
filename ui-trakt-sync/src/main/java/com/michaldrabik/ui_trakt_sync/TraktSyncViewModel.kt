package com.michaldrabik.ui_trakt_sync

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventsManager
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
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.TraktSyncSchedule
import com.michaldrabik.ui_trakt_sync.cases.TraktSyncRatingsCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class TraktSyncViewModel @Inject constructor(
  @Named("miscPreferences") private var miscPreferences: SharedPreferences,
  private val userManager: UserTraktManager,
  private val workManager: WorkManager,
  private val ratingsCase: TraktSyncRatingsCase,
  private val settingsRepository: SettingsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val eventsManager: EventsManager,
  importWatchedRunner: TraktImportWatchedRunner,
  importWatchlistRunner: TraktImportWatchlistRunner,
  exportWatchedRunner: TraktImportWatchedRunner,
  exportWatchlistRunner: TraktExportWatchlistRunner,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val progressState = MutableStateFlow(false)
  private val progressStatusState = MutableStateFlow("")
  private val authorizedState = MutableStateFlow(false)
  private val authErrorState = MutableStateFlow(false)
  private val traktSyncScheduleState = MutableStateFlow(TraktSyncSchedule.OFF)
  private val quickSyncEnabledState = MutableStateFlow(false)
  private val dateFormatState = MutableStateFlow<DateTimeFormatter?>(null)
  private val traktSyncTimestampState = MutableStateFlow(0L)

  init {
    viewModelScope.launch {
      eventsManager.events.collect { handleEvent(it) }
    }

    val runners = listOf(
      importWatchedRunner,
      importWatchlistRunner,
      exportWatchedRunner,
      exportWatchlistRunner
    )
    if (runners.any { it.isRunning }) {
      progressState.value = true
    }
  }

  fun invalidate() {
    viewModelScope.launch {
      val settings = settingsRepository.load()

      authorizedState.value = userManager.isAuthorized()
      traktSyncScheduleState.value = settings.traktSyncSchedule
      quickSyncEnabledState.value = settings.traktQuickSyncEnabled
      dateFormatState.value = dateFormatProvider.loadFullHourFormat()
      traktSyncTimestampState.value = miscPreferences.getLong(TraktSyncService.KEY_LAST_SYNC_TIMESTAMP, 0)
    }
  }

  fun authorizeTrakt(code: String?) {
    viewModelScope.launch {
      try {
        if (code.isNullOrBlank()) {
          throw IllegalStateException("Invalid Trakt authorization code.")
        }
        userManager.authorize(code)
        messageChannel.send(MessageEvent.Info(R.string.textTraktLoginSuccess))
        invalidate()
        saveTraktQuickRemove()
        preloadRatings()
      } catch (error: Throwable) {
        val message = when {
          error is HttpException && error.code() == 423 -> R.string.errorTraktLocked
          else -> R.string.errorAuthorization
        }
        messageChannel.send(MessageEvent.Error(message))
      }
    }
  }

  fun saveTraktSyncSchedule(schedule: TraktSyncSchedule) {
    viewModelScope.launch {
      val settings = settingsRepository.load()
      settings.let {
        val new = it.copy(traktSyncSchedule = schedule)
        settingsRepository.update(new)
      }
      TraktSyncWorker.schedule(workManager, schedule, cancelExisting = true)
      traktSyncScheduleState.value = schedule
    }
  }

  private fun saveTraktQuickRemove() {
    viewModelScope.launch {
      val settings = settingsRepository.load()
      settings.let {
        val new = it.copy(traktQuickRemoveEnabled = true)
        settingsRepository.update(new)
      }
    }
  }

  private fun preloadRatings() {
    viewModelScope.launch {
      try {
        ratingsCase.preloadRatings()
      } catch (error: Throwable) {
        Timber.e("Failed to preload some of ratings")
        rethrowCancellation(error)
      }
    }
  }

  fun handleEvent(event: Event) {
    viewModelScope.launch {
      when (event) {
        is TraktSyncStart -> {
          progressState.value = true
          progressStatusState.value = ""
          messageChannel.send(MessageEvent.Info(R.string.textTraktSyncStarted))
        }
        is TraktSyncProgress -> {
          progressState.value = true
          progressStatusState.value = event.status
        }
        is TraktSyncSuccess -> {
          progressState.value = false
          progressStatusState.value = ""
          messageChannel.send(MessageEvent.Info(R.string.textTraktSyncComplete))
        }
        is TraktSyncError -> {
          progressState.value = false
          progressStatusState.value = ""
          messageChannel.send(MessageEvent.Info(R.string.textTraktSyncError))
        }
        is TraktSyncAuthError -> {
          viewModelScope.launch {
            progressState.value = false
            progressStatusState.value = ""
            authErrorState.value = true
            messageChannel.send(MessageEvent.Error(R.string.errorTraktAuthorization))
          }
        }
        else -> Timber.d("Unsupported sync event")
      }
    }
  }

  val uiState = combine(
    progressState,
    progressStatusState,
    authorizedState,
    authErrorState,
    traktSyncScheduleState,
    quickSyncEnabledState,
    dateFormatState,
    traktSyncTimestampState
  ) {
    progressState, progressStatusState, authorizedState, authErrorState, traktSyncScheduleState, quickSyncEnabledState, dateFormatState,
    traktSyncTimestampState,
    ->
    TraktSyncUiState(
      isProgress = progressState,
      progressStatus = progressStatusState,
      isAuthorized = authorizedState,
      authError = authErrorState,
      traktSyncSchedule = traktSyncScheduleState,
      quickSyncEnabled = quickSyncEnabledState,
      dateFormat = dateFormatState,
      lastTraktSyncTimestamp = traktSyncTimestampState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = TraktSyncUiState()
  )
}
