package com.michaldrabik.ui_progress_movies.calendar.helpers.groupers

import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import java.time.ZonedDateTime

interface CalendarGrouper {
  fun groupByTime(
    nowUtc: ZonedDateTime,
    items: List<CalendarMovieListItem.MovieItem>
  ): List<CalendarMovieListItem>
}
