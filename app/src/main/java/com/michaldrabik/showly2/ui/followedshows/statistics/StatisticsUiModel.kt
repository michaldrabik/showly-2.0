package com.michaldrabik.showly2.ui.followedshows.statistics

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.followedshows.statistics.views.mostWatched.StatisticsMostWatchedItem

data class StatisticsUiModel(
  val mostWatchedShows: List<StatisticsMostWatchedItem>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as StatisticsUiModel).copy(
      mostWatchedShows = newModel.mostWatchedShows?.toList() ?: mostWatchedShows
    )
}
