package com.michaldrabik.showly2.ui.watchlist.pages.upcoming

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem

data class WatchlistUpcomingUiModel(
  val items: List<WatchlistItem>? = null,
  val isSearching: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as WatchlistUpcomingUiModel).copy(
      items = newModel.items?.toList() ?: items,
      isSearching = newModel.isSearching ?: isSearching
    )
}
