package com.michaldrabik.ui_progress_movies.calendar.helpers.sorter

import com.michaldrabik.ui_model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRecentsSorter @Inject constructor() : CalendarSorter {
  override fun sort() = compareByDescending<Movie> { it.released }.thenByDescending { it.year }
}
