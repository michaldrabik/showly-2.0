package com.michaldrabik.ui_my_shows.watchlist

import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem

data class WatchlistUiState(
  val items: List<CollectionListItem> = emptyList(),
  val viewMode: ListViewMode = ListViewMode.LIST_NORMAL,
  val resetScroll: Event<Boolean>? = null,
  val sortOrder: Event<Pair<SortOrder, SortType>>? = null,
)
