package com.michaldrabik.ui_watchlist.upcoming

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_watchlist.WatchlistItem

data class WatchlistUpcomingUiModel(
  val items: List<WatchlistItem>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as WatchlistUpcomingUiModel).copy(
      items = newModel.items?.toList() ?: items
    )
}
