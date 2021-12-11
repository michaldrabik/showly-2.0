package com.michaldrabik.ui_model

import com.michaldrabik.ui_model.MovieStatus.IN_PRODUCTION
import com.michaldrabik.ui_model.MovieStatus.PLANNED
import com.michaldrabik.ui_model.MovieStatus.POST_PRODUCTION
import com.michaldrabik.ui_model.MovieStatus.RUMORED
import java.time.LocalDate
import java.time.ZonedDateTime

data class PersonCredit(
  val show: Show?,
  val movie: Movie?,
  val image: Image,
  val translation: Translation?
) {

  fun requireShow() = show!!
  fun requireMovie() = movie!!

  val releaseDate: LocalDate?
    get() = when {
      show != null ->
        if (show.firstAired.isNotBlank()) {
          ZonedDateTime.parse(show.firstAired).toLocalDate()
        } else {
          null
        }
      movie != null -> movie.released
      else -> null
    }

  val isUpcoming: Boolean
    get() = when {
      show != null -> {
        show.status in arrayOf(ShowStatus.IN_PRODUCTION, ShowStatus.PLANNED, ShowStatus.UPCOMING)
      }
      movie != null -> {
        !movie.hasAired() && movie.status in arrayOf(RUMORED, PLANNED, IN_PRODUCTION, POST_PRODUCTION)
      }
      else -> false
    }
}
