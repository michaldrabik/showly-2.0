package com.michaldrabik.ui_discover_movies.recycler

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieImage
import com.michaldrabik.ui_model.Translation

data class DiscoverMovieListItem(
  override val movie: Movie,
  override val image: MovieImage,
  override var isLoading: Boolean = false,
  val isCollected: Boolean = false,
  val isWatchlist: Boolean = false,
  val translation: Translation? = null
) : MovieListItem
