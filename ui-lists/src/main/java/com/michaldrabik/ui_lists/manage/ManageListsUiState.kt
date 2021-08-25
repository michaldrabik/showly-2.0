package com.michaldrabik.ui_lists.manage

import com.michaldrabik.ui_lists.manage.recycler.ManageListsItem

data class ManageListsUiState(
  val items: List<ManageListsItem>? = null,
)
