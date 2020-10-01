package com.michaldrabik.ui_statistics

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_statistics.views.mostWatched.StatisticsMostWatchedItem
import com.michaldrabik.ui_statistics.views.ratings.recycler.StatisticsRatingItem

data class StatisticsUiModel(
  val mostWatchedShows: List<StatisticsMostWatchedItem>? = null,
  val mostWatchedTotalCount: Int? = null,
  val totalTimeSpentMinutes: Long? = null,
  val totalWatchedEpisodes: Long? = null,
  val totalWatchedEpisodesShows: Long? = null,
  val topGenres: List<String>? = null,
  val ratings: List<StatisticsRatingItem>? = null,
  val archivedShowsIncluded: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as StatisticsUiModel).copy(
      mostWatchedShows = newModel.mostWatchedShows?.toList() ?: mostWatchedShows,
      mostWatchedTotalCount = newModel.mostWatchedTotalCount ?: mostWatchedTotalCount,
      totalTimeSpentMinutes = newModel.totalTimeSpentMinutes ?: totalTimeSpentMinutes,
      totalWatchedEpisodes = newModel.totalWatchedEpisodes ?: totalWatchedEpisodes,
      totalWatchedEpisodesShows = newModel.totalWatchedEpisodesShows ?: totalWatchedEpisodesShows,
      topGenres = newModel.topGenres?.toList() ?: topGenres,
      ratings = newModel.ratings?.toList() ?: ratings,
      archivedShowsIncluded = newModel.archivedShowsIncluded ?: archivedShowsIncluded
    )
}
