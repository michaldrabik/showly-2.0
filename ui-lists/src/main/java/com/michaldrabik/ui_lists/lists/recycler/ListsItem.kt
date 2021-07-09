package com.michaldrabik.ui_lists.lists.recycler

import com.michaldrabik.ui_lists.lists.helpers.ListsItemImage
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.SortOrder
import java.time.format.DateTimeFormatter

data class ListsItem(
  val list: CustomList,
  val images: List<ListsItemImage>,
  val sortOrder: SortOrder,
  val dateFormat: DateTimeFormatter? = null
)
