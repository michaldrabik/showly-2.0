package com.michaldrabik.ui_movie.related

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie

data class RelatedListItem(
  override val movie: Movie,
  override val image: Image,
  override var isLoading: Boolean = false
) : MovieListItem
