package com.michaldrabik.ui_statistics_movies

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem

data class StatisticsMoviesUiModel(
  val totalTimeSpentMinutes: Long? = null,
  val totalWatchedMovies: Int? = null,
  val topGenres: List<Genre>? = null,
  val ratings: List<StatisticsMoviesRatingItem>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as StatisticsMoviesUiModel).copy(
      totalTimeSpentMinutes = newModel.totalTimeSpentMinutes ?: totalTimeSpentMinutes,
      totalWatchedMovies = newModel.totalWatchedMovies ?: totalWatchedMovies,
      topGenres = newModel.topGenres?.toList() ?: topGenres,
      ratings = newModel.ratings?.toList() ?: ratings
    )
}
