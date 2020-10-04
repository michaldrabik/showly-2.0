package com.michaldrabik.ui_my_shows.myshows

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem

data class MyShowsUiModel(
  val listItems: List<MyShowsItem>? = null,
  val notifyListsUpdate: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as MyShowsUiModel).copy(
      listItems = newModel.listItems?.toList() ?: listItems,
      notifyListsUpdate = newModel.notifyListsUpdate ?: notifyListsUpdate
    )
}
