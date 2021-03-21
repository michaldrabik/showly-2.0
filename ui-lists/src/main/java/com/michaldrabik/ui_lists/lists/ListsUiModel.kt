package com.michaldrabik.ui_lists.lists

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.SortOrder

data class ListsUiModel(
  val items: List<ListsItem>? = null,
  val resetScroll: ActionEvent<Boolean>? = null,
  val sortOrderEvent: ActionEvent<SortOrder>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ListsUiModel)
      .copy(
        items = newModel.items?.toList() ?: items,
        resetScroll = newModel.resetScroll ?: resetScroll,
        sortOrderEvent = newModel.sortOrderEvent ?: sortOrderEvent
      )
}
