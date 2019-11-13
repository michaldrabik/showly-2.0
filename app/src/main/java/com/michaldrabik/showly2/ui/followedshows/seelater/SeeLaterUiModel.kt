package com.michaldrabik.showly2.ui.followedshows.seelater

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterListItem

data class SeeLaterUiModel(
  val items: List<SeeLaterListItem>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SeeLaterUiModel).copy(
      items = newModel.items ?: items
    )
}