package com.michaldrabik.ui_base.common

import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieImage

interface MovieListItem {
  val movie: Movie
  val image: MovieImage
  val isLoading: Boolean

  fun isSameAs(other: MovieListItem) = movie.ids.trakt == other.movie.ids.trakt
}
