package com.michaldrabik.ui_progress_movies.calendar.helpers.sorter

import com.michaldrabik.ui_model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarFutureSorter @Inject constructor() : CalendarSorter {
  override fun sort() = compareBy<Movie> { it.released }.thenBy { it.year }
}
