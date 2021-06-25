package com.michaldrabik.ui_progress.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress.main.cases.ProgressMainEpisodesCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressMainViewModel @Inject constructor(
  private val episodesCase: ProgressMainEpisodesCase,
) : BaseViewModel<ProgressMainUiModel>() {

  private var calendarMode = CalendarMode.PRESENT_FUTURE

  fun loadProgress() {
    viewModelScope.launch {
      uiState = ProgressMainUiModel(
        timestamp = System.currentTimeMillis(),
        calendarMode = calendarMode
      )
    }
  }

  fun onSearchQuery(searchQuery: String) {
    uiState = ProgressMainUiModel(searchQuery = searchQuery)
  }

  fun toggleCalendarMode() {
    calendarMode = when (calendarMode) {
      CalendarMode.PRESENT_FUTURE -> CalendarMode.RECENTS
      CalendarMode.RECENTS -> CalendarMode.PRESENT_FUTURE
    }
    uiState = ProgressMainUiModel(calendarMode = calendarMode)
  }

  fun setWatchedEpisode(context: Context, bundle: EpisodeBundle) {
    viewModelScope.launch {
      if (!bundle.episode.hasAired(bundle.season)) {
        _messageLiveData.value = MessageEvent.info(R.string.errorEpisodeNotAired)
        return@launch
      }
      episodesCase.setEpisodeWatched(context, bundle)
      uiState = ProgressMainUiModel(
        timestamp = System.currentTimeMillis(),
        resetScroll = ActionEvent(true)
      )
    }
  }
}
