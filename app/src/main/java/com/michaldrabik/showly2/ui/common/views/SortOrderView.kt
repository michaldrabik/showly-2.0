package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.DATE_ADDED
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_sort_order.view.*

class SortOrderView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var sortSelectedListener: (SortOrder) -> Unit = {}

  init {
    inflate(context, R.layout.view_sort_order, this)
    orientation = VERTICAL
    setBackgroundResource(R.color.colorSortViewBackground)

    sortOrderName.onClick { sortSelectedListener(NAME) }
    sortOrderNewest.onClick { sortSelectedListener(NEWEST) }
    sortOrderRating.onClick { sortSelectedListener(RATING) }
    sortOrderDateAdded.onClick { sortSelectedListener(DATE_ADDED) }
  }

  fun setAvailable(sortOrders: List<SortOrder>) {
    sortOrderName.visibleIf(NAME in sortOrders)
    sortOrderNewest.visibleIf(NEWEST in sortOrders)
    sortOrderRating.visibleIf(RATING in sortOrders)
    sortOrderDateAdded.visibleIf(DATE_ADDED in sortOrders)
  }

  fun bind(sortOrder: SortOrder) {
    sortOrderNameCheck.visibleIf(sortOrder == NAME, false)
    sortOrderNewestCheck.visibleIf(sortOrder == NEWEST, false)
    sortOrderRatingCheck.visibleIf(sortOrder == RATING, false)
    sortOrderDateAddedCheck.visibleIf(sortOrder == DATE_ADDED, false)
  }
}