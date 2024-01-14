package com.michaldrabik.ui_lists.manage.recycler

import com.michaldrabik.ui_model.CustomList

data class ManageListsItem(
  val list: CustomList,
  val isChecked: Boolean,
  val isEnabled: Boolean
)
