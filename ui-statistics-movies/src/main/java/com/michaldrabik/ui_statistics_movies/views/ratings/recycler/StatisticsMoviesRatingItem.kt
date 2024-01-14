package com.michaldrabik.ui_statistics_movies.views.ratings.recycler

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.TraktRating

data class StatisticsMoviesRatingItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean,
  val rating: TraktRating
) : MovieListItem
