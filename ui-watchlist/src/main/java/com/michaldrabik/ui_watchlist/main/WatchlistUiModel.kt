package com.michaldrabik.ui_watchlist.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_watchlist.WatchlistItem

data class WatchlistUiModel(
  val items: List<WatchlistItem>? = null,
  val isSearching: Boolean? = null,
  val resetScroll: Boolean? = null,
  val sortOrder: SortOrder? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as WatchlistUiModel).copy(
      items = newModel.items?.toList() ?: items,
      isSearching = newModel.isSearching ?: isSearching,
      resetScroll = newModel.resetScroll ?: resetScroll,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
