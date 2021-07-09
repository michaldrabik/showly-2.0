package com.michaldrabik.ui_progress_movies.calendar.helpers.filters

import com.michaldrabik.ui_model.Movie
import java.time.ZonedDateTime

interface CalendarFilter {
  fun filter(now: ZonedDateTime, movie: Movie): Boolean
}
