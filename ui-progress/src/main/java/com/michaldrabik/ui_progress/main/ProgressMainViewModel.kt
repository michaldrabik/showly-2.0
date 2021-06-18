package com.michaldrabik.ui_progress.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
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

  private var searchQuery = ""
  private var calendarMode = CalendarMode.PRESENT_FUTURE

  private val _searchQueryLiveData = MutableLiveData<String>()
  private val _calendarModeLiveData = MutableLiveData<CalendarMode>()

  val searchQueryLiveData: LiveData<String> get() = _searchQueryLiveData
  val calendarModeLiveData: LiveData<CalendarMode> get() = _calendarModeLiveData

  fun loadProgress() {
    viewModelScope.launch {
      _searchQueryLiveData.value = searchQuery
      _calendarModeLiveData.value = calendarMode
    }
  }

  fun onSearchQuery(searchQuery: String) {
    this.searchQuery = searchQuery.trim()
    loadProgress()
  }

  fun toggleCalendarMode() {
    calendarMode = when (calendarMode) {
      CalendarMode.PRESENT_FUTURE -> CalendarMode.RECENTS
      CalendarMode.RECENTS -> CalendarMode.PRESENT_FUTURE
    }
    _calendarModeLiveData.value = calendarMode
  }

  fun setWatchedEpisode(context: Context, bundle: EpisodeBundle) {
    viewModelScope.launch {
      if (!bundle.episode.hasAired(bundle.season)) {
        _messageLiveData.value = MessageEvent.info(R.string.errorEpisodeNotAired)
        return@launch
      }
      episodesCase.setEpisodeWatched(context, bundle)
      loadProgress()
    }
  }
}
