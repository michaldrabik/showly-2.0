package com.michaldrabik.ui_progress_movies.calendar

import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem

data class CalendarMoviesUiState(
  val items: List<CalendarMovieListItem>? = null,
  val mode: CalendarMode = CalendarMode.PRESENT_FUTURE,
)
