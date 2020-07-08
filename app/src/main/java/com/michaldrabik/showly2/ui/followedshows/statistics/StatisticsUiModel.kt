package com.michaldrabik.showly2.ui.followedshows.statistics

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterListItem

data class StatisticsUiModel(
  val items: List<SeeLaterListItem>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as StatisticsUiModel).copy(
      items = newModel.items ?: items
    )
}
