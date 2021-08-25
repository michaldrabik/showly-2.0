package com.michaldrabik.ui_my_movies.watchlist.recycler

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

data class WatchlistListItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean = false,
  val translation: Translation? = null,
  val userRating: Int? = null,
  val dateFormat: DateTimeFormatter? = null
) : MovieListItem
