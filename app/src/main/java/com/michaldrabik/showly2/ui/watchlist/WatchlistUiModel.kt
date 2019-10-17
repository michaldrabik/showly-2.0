package com.michaldrabik.showly2.ui.watchlist

import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem

data class WatchlistUiModel(
  val watchlistItems: List<WatchlistItem>? = null,
  val error: Error? = null
)