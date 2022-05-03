package com.michaldrabik.ui_movie.sections.related.recycler

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie

data class RelatedListItem(
  override val movie: Movie,
  override val image: Image,
  override var isLoading: Boolean = false,
  val isFollowed: Boolean = false,
  val isWatchlist: Boolean = false
) : MovieListItem
