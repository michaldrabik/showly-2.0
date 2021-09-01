package com.michaldrabik.ui_lists.lists

import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.SortOrder

data class ListsUiState(
  val items: List<ListsItem>? = null,
  val resetScroll: Event<Boolean> = Event(false),
  val sortOrder: Event<SortOrder>? = null,
)
