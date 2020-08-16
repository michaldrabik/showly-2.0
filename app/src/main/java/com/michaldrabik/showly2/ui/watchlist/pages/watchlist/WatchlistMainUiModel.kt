package com.michaldrabik.showly2.ui.watchlist.pages.watchlist

import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem

data class WatchlistMainUiModel(
  val items: List<WatchlistItem>? = null,
  val isSearching: Boolean? = null,
  val resetScroll: Boolean? = null,
  val sortOrder: SortOrder? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as WatchlistMainUiModel).copy(
      items = newModel.items?.toList() ?: items,
      isSearching = newModel.isSearching ?: isSearching,
      resetScroll = newModel.resetScroll ?: resetScroll,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
