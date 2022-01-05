package com.michaldrabik.ui_base.common.sheets.context_menu.movie.helpers

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

data class MovieContextItem(
  val movie: Movie,
  val image: Image,
  val translation: Translation?,
  val dateFormat: DateTimeFormatter?,
  val userRating: Int?,
  val isMyMovie: Boolean,
  val isWatchlist: Boolean,
  val isHidden: Boolean,
  val isPinnedTop: Boolean
) {

  fun isInCollection() = isHidden || isWatchlist || isMyMovie
}
