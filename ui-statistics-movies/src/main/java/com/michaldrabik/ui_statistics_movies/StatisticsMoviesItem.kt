package com.michaldrabik.ui_statistics_movies

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation

data class StatisticsMoviesItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean = false,
  val translation: Translation? = null
) : MovieListItem
