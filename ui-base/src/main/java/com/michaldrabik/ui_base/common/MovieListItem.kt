package com.michaldrabik.ui_base.common

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie

interface MovieListItem {
  val movie: Movie
  val image: Image
  val isLoading: Boolean

  infix fun isSameAs(other: MovieListItem) = movie.ids.trakt == other.movie.ids.trakt
}
