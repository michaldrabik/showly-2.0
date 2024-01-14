package com.michaldrabik.ui_my_shows.myshows

import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem

data class MyShowsUiState(
  val items: List<MyShowsItem>? = null,
  val showEmptyView: Boolean = false,
  val viewMode: ListViewMode = ListViewMode.LIST_NORMAL,
  val resetScrollMap: Event<List<MyShowsItem.Type>?>? = null,
)
