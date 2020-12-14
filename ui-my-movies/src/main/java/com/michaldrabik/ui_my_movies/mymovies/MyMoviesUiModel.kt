package com.michaldrabik.ui_my_movies.mymovies

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem

data class MyMoviesUiModel(
  val listItems: List<MyMoviesItem>? = null,
  val notifyListsUpdate: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as MyMoviesUiModel).copy(
      listItems = newModel.listItems?.toList() ?: listItems,
      notifyListsUpdate = newModel.notifyListsUpdate ?: notifyListsUpdate
    )
}
