package com.michaldrabik.ui_lists.lists

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.SortOrder

data class ListsUiState(
  val items: List<ListsItem>? = null,
  val resetScroll: ActionEvent<Boolean> = ActionEvent(false),
  val sortOrder: ActionEvent<SortOrder>? = null,
)
