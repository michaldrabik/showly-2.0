package com.michaldrabik.ui_statistics_movies

import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem

data class StatisticsMoviesUiState(
  val totalTimeSpentMinutes: Int? = null,
  val totalWatchedMovies: Int? = null,
  val topGenres: List<Genre>? = null,
  val ratings: List<StatisticsMoviesRatingItem>? = null,
)
