package com.michaldrabik.ui_base.common

import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType

interface OnSortOrderChangeListener {
  fun onSortOrderChange(
    sortOrder: SortOrder,
    sortType: SortType
  )
}
