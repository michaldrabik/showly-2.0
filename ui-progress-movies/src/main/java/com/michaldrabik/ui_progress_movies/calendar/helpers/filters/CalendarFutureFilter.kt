package com.michaldrabik.ui_progress_movies.calendar.helpers.filters

import com.michaldrabik.ui_model.Movie
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarFutureFilter @Inject constructor() : CalendarFilter {

  override fun filter(now: ZonedDateTime, movie: Movie) =
    movie.released?.isAfter(now.toLocalDate()) == true || movie.released?.dayOfYear == now.dayOfYear
}
