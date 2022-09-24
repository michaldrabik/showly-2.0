package com.michaldrabik.ui_lists.lists

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType

data class ListsUiState(
  val items: List<ListsItem>? = null,
  val resetScroll: Event<Boolean> = Event(false),
  val sortOrder: Pair<SortOrder, SortType>? = null,
  val isSyncing: Boolean? = null,
)
