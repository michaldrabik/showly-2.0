package com.michaldrabik.showly2.ui.followedshows.statistics

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.followedshows.statistics.views.mostWatched.StatisticsMostWatchedItem

data class StatisticsUiModel(
  val mostWatchedShows: List<StatisticsMostWatchedItem>? = null,
  val mostWatchedTotalCount: Int? = null,
  val totalTimeSpentMinutes: Long? = null,
  val totalWatchedEpisodes: Long? = null,
  val totalWatchedEpisodesShows: Long? = null,
  val topGenres: List<String>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as StatisticsUiModel).copy(
      mostWatchedShows = newModel.mostWatchedShows?.toList() ?: mostWatchedShows,
      mostWatchedTotalCount = newModel.mostWatchedTotalCount ?: mostWatchedTotalCount,
      totalTimeSpentMinutes = newModel.totalTimeSpentMinutes ?: totalTimeSpentMinutes,
      totalWatchedEpisodes = newModel.totalWatchedEpisodes ?: totalWatchedEpisodes,
      totalWatchedEpisodesShows = newModel.totalWatchedEpisodesShows ?: totalWatchedEpisodesShows,
      topGenres = newModel.topGenres?.toList() ?: topGenres
    )
}
