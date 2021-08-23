package com.michaldrabik.ui_progress_movies.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesMainCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressMoviesMainViewModel @Inject constructor(
  private val moviesCase: ProgressMoviesMainCase,
) : BaseViewModel() {

  private val timestampState = MutableStateFlow<Long?>(null)
  private val searchQueryState = MutableStateFlow<String?>(null)
  private val calendarModeState = MutableStateFlow<CalendarMode?>(null)

  val uiState = combine(
    timestampState,
    searchQueryState,
    calendarModeState
  ) { s1, s2, s3 ->
    ProgressMoviesMainUiState(
      timestamp = s1,
      searchQuery = s2,
      calendarMode = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ProgressMoviesMainUiState()
  )

  private var calendarMode = CalendarMode.PRESENT_FUTURE

  fun loadProgress() {
    viewModelScope.launch {
      timestampState.value = System.currentTimeMillis()
      calendarModeState.value = calendarMode
    }
  }

  fun onSearchQuery(searchQuery: String) {
    searchQueryState.value = searchQuery
  }

  fun toggleCalendarMode() {
    calendarMode = when (calendarMode) {
      CalendarMode.PRESENT_FUTURE -> CalendarMode.RECENTS
      CalendarMode.RECENTS -> CalendarMode.PRESENT_FUTURE
    }
    calendarModeState.value = calendarMode
  }

  fun setWatchedMovie(context: Context, movie: Movie) {
    viewModelScope.launch {
      moviesCase.addToMyMovies(context, movie)
      timestampState.value = System.currentTimeMillis()
    }
  }
}
