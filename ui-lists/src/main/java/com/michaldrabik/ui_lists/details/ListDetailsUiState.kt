package com.michaldrabik.ui_lists.details

import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.CustomList

data class ListDetailsUiState(
  val listDetails: CustomList? = null,
  val listItems: List<ListDetailsItem>? = null,
  val resetScroll: Event<Boolean>? = null,
  val deleteEvent: Event<Boolean>? = null,
  val isFiltersVisible: Boolean = false,
  val isManageMode: Boolean = false,
  val isQuickRemoveEnabled: Boolean = false,
  val isLoading: Boolean = false,
  val viewMode: ListViewMode = ListViewMode.LIST_NORMAL
)
