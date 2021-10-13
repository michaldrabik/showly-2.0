package com.michaldrabik.ui_statistics

import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_statistics.views.mostWatched.StatisticsMostWatchedItem
import com.michaldrabik.ui_statistics.views.ratings.recycler.StatisticsRatingItem

data class StatisticsUiState(
  val mostWatchedShows: List<StatisticsMostWatchedItem>? = null,
  val mostWatchedTotalCount: Int? = null,
  val totalTimeSpentMinutes: Int? = null,
  val totalWatchedEpisodes: Int? = null,
  val totalWatchedEpisodesShows: Int? = null,
  val topGenres: List<Genre>? = null,
  val ratings: List<StatisticsRatingItem>? = null,
)
