package com.michaldrabik.showly2.ui.watchlist

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.recycler.WatchlistMainItem

data class WatchlistUiModel(
  val items: List<WatchlistMainItem>? = null,
  val isSearching: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as WatchlistUiModel).copy(
      items = newModel.items?.toList() ?: items,
      isSearching = newModel.isSearching ?: isSearching
    )
}
