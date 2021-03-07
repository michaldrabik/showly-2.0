package com.michaldrabik.ui_lists.my_lists

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_lists.my_lists.recycler.MyListItem

data class MyListsUiModel(
  val items: List<MyListItem>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as MyListsUiModel)
      .copy(
        items = newModel.items?.toList() ?: items
      )
}
