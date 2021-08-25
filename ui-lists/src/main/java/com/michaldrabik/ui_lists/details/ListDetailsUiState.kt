package com.michaldrabik.ui_lists.details

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.CustomList

data class ListDetailsUiState(
  val listDetails: CustomList? = null,
  val listItems: List<ListDetailsItem>? = null,
  val resetScroll: ActionEvent<Boolean>? = null,
  val deleteEvent: ActionEvent<Boolean>? = null,
  val isManageMode: Boolean = false,
  val isQuickRemoveEnabled: Boolean = false,
  val isLoading: Boolean = false,
)
