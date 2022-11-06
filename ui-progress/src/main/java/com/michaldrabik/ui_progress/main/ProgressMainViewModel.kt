package com.michaldrabik.ui_progress.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktSyncAuthError
import com.michaldrabik.ui_base.events.TraktSyncError
import com.michaldrabik.ui_base.events.TraktSyncSuccess
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.main.cases.ProgressMainEpisodesCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.michaldrabik.ui_base.events.Event as EventSync

@HiltViewModel
class ProgressMainViewModel @Inject constructor(
  private val episodesCase: ProgressMainEpisodesCase,
  private val eventsManager: EventsManager,
  workManager: WorkManager,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val timestampState = MutableStateFlow<Long?>(null)
  private val searchQueryState = MutableStateFlow<String?>(null)
  private val calendarModeState = MutableStateFlow<CalendarMode?>(null)
  private val scrollState = MutableStateFlow<Event<Boolean>?>(null)
  private val syncingState = MutableStateFlow(false)

  private var calendarMode = CalendarMode.PRESENT_FUTURE

  init {
    viewModelScope.launch {
      eventsManager.events.collect { onEvent(it) }
    }
    workManager.getWorkInfosByTagLiveData(TraktSyncWorker.TAG_ID).observeForever { work ->
      syncingState.value = work.any { it.state == WorkInfo.State.RUNNING }
    }
  }

  fun loadProgress() {
    viewModelScope.launch {
      timestampState.value = System.currentTimeMillis()
      calendarModeState.value = calendarMode
    }
  }

  fun onSearchQuery(searchQuery: String?) {
    searchQueryState.value = searchQuery ?: ""
  }

  fun toggleCalendarMode() {
    calendarMode = when (calendarMode) {
      CalendarMode.PRESENT_FUTURE -> CalendarMode.RECENTS
      CalendarMode.RECENTS -> CalendarMode.PRESENT_FUTURE
    }
    calendarModeState.value = calendarMode
  }

  fun setWatchedEpisode(bundle: EpisodeBundle) {
    viewModelScope.launch {
      if (!bundle.episode.hasAired(bundle.season)) {
        messageChannel.send(MessageEvent.Info(R.string.errorEpisodeNotAired))
        return@launch
      }
      episodesCase.setEpisodeWatched(bundle)
      timestampState.value = System.currentTimeMillis()
      scrollState.value = Event(false)
    }
  }

  private fun onEvent(event: EventSync) {
    if (event in arrayOf(TraktSyncError, TraktSyncAuthError, TraktSyncSuccess)) {
      loadProgress()
    }
  }

  val uiState = combine(
    timestampState,
    searchQueryState,
    calendarModeState,
    scrollState,
    syncingState
  ) { s1, s2, s3, s4, s5 ->
    ProgressMainUiState(
      timestamp = s1,
      searchQuery = s2,
      calendarMode = s3,
      resetScroll = s4,
      isSyncing = s5
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ProgressMainUiState()
  )
}
