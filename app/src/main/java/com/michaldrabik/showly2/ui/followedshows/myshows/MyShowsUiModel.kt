package com.michaldrabik.showly2.ui.followedshows.myshows

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem

data class MyShowsUiModel(
  val listItems: List<MyShowsItem>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as MyShowsUiModel).copy(
      listItems = newModel.listItems?.toList() ?: listItems
    )
}
