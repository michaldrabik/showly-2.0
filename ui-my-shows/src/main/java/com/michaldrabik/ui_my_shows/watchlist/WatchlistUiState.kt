package com.michaldrabik.ui_my_shows.watchlist

import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistListItem

data class WatchlistUiState(
  val items: List<WatchlistListItem> = emptyList(),
  val resetScroll: Event<Boolean>? = null,
  val sortOrder: Event<SortOrder>? = null
)
