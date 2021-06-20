package com.michaldrabik.ui_progress_movies.calendar.helpers.groupers

import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem

interface CalendarGrouper {
  fun groupByTime(items: List<CalendarMovieListItem.MovieItem>): List<CalendarMovieListItem>
}
