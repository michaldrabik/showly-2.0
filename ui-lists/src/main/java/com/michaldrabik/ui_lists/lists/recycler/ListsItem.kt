package com.michaldrabik.ui_lists.lists.recycler

import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrder
import org.threeten.bp.format.DateTimeFormatter

data class ListsItem(
  val list: CustomList,
  val images: List<Image>,
  val sortOrder: SortOrder,
  val dateFormat: DateTimeFormatter? = null
)
