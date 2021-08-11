package com.michaldrabik.ui_my_shows.watchlist

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistListItem

data class WatchlistUiState(
  val items: List<WatchlistListItem> = emptyList(),
  val resetScroll: ActionEvent<Boolean>? = null,
  val sortOrder: ActionEvent<SortOrder>? = null
)
