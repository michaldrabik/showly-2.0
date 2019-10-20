package com.michaldrabik.showly2.ui.watchlist

import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem

data class WatchlistUiModel(
  val updateListItem: WatchlistItem? = null,
  val info: Int? = null,
  val error: Error? = null
)