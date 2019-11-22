package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem

data class DiscoverUiModel(
  val shows: List<DiscoverListItem>? = null,
  val showLoading: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as DiscoverUiModel).copy(
      shows = newModel.shows ?: shows,
      showLoading = newModel.showLoading ?: showLoading
    )
}