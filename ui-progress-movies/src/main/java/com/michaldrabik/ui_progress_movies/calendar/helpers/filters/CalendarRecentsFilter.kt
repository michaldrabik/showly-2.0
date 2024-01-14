package com.michaldrabik.ui_progress_movies.calendar.helpers.filters

import com.michaldrabik.ui_model.Movie
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRecentsFilter @Inject constructor() : CalendarFilter {

  override fun filter(now: ZonedDateTime, movie: Movie) =
    movie.released?.isBefore(now.toLocalDate()) == true
}
