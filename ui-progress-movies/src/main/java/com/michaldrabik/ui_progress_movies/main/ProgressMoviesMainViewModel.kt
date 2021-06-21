package com.michaldrabik.ui_progress_movies.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesMainCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressMoviesMainViewModel @Inject constructor(
  private val moviesCase: ProgressMoviesMainCase,
) : BaseViewModel<ProgressMoviesMainUiModel>() {

  private var calendarMode = CalendarMode.PRESENT_FUTURE

  fun loadProgress() {
    viewModelScope.launch {
      uiState = ProgressMoviesMainUiModel(
        timestamp = System.currentTimeMillis(),
        calendarMode = calendarMode
      )
    }
  }

  fun onSearchQuery(searchQuery: String) {
    uiState = ProgressMoviesMainUiModel(searchQuery = searchQuery)
  }

  fun toggleCalendarMode() {
    calendarMode = when (calendarMode) {
      CalendarMode.PRESENT_FUTURE -> CalendarMode.RECENTS
      CalendarMode.RECENTS -> CalendarMode.PRESENT_FUTURE
    }
    uiState = ProgressMoviesMainUiModel(calendarMode = calendarMode)
  }

  fun setWatchedMovie(context: Context, movie: Movie) {
    viewModelScope.launch {
      moviesCase.addToMyMovies(context, movie)
      uiState = ProgressMoviesMainUiModel(timestamp = System.currentTimeMillis())
    }
  }
}
