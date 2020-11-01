package com.michaldrabik.ui_my_shows.seelater

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.seelater.recycler.SeeLaterListItem

data class SeeLaterUiModel(
  val items: List<SeeLaterListItem>? = null,
  val scrollToTop: ActionEvent<Boolean>? = null,
  val sortOrder: ActionEvent<SortOrder>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SeeLaterUiModel).copy(
      items = newModel.items ?: items,
      scrollToTop = newModel.scrollToTop ?: scrollToTop,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
