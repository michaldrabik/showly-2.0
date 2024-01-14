package com.michaldrabik.ui_search.utilities

import com.michaldrabik.common.Mode
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType

data class SearchOptions(
  val filters: List<Mode> = emptyList(),
  val sortOrder: SortOrder = SortOrder.RANK,
  val sortType: SortType = SortType.ASCENDING
)
