package com.michaldrabik.ui_my_movies.watchlist

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistListItem

data class WatchlistUiModel(
  val items: List<WatchlistListItem>? = null,
  val resetScroll: ActionEvent<Boolean>? = null,
  val sortOrder: ActionEvent<SortOrder>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as WatchlistUiModel).copy(
      items = newModel.items ?: items,
      resetScroll = newModel.resetScroll ?: resetScroll,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
