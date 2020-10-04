package com.michaldrabik.ui_my_shows.seelater

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.seelater.recycler.SeeLaterListItem

data class SeeLaterUiModel(
  val items: List<SeeLaterListItem>? = null,
  val sortOrder: SortOrder? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SeeLaterUiModel).copy(
      items = newModel.items ?: items,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
