package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.DiscoverSortOrder
import com.michaldrabik.showly2.model.DiscoverSortOrder.HOT
import com.michaldrabik.showly2.model.DiscoverSortOrder.NEWEST
import com.michaldrabik.showly2.model.DiscoverSortOrder.RATING
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_discover_sort_order.view.*

class DiscoverSortOrderView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var sortSelectedListener: (DiscoverSortOrder) -> Unit = {}

  init {
    inflate(context, R.layout.view_discover_sort_order, this)
    orientation = VERTICAL
    setBackgroundResource(R.drawable.bg_sort_view)

    discoverSortOrderHot.onClick { sortSelectedListener(HOT) }
    discoverSortOrderNewest.onClick { sortSelectedListener(NEWEST) }
    discoverSortOrderRating.onClick { sortSelectedListener(RATING) }
  }

  fun bind(sortOrder: DiscoverSortOrder) {
    discoverSortOrderHotCheck.visibleIf(sortOrder == HOT, false)
    discoverSortOrderNewestCheck.visibleIf(sortOrder == NEWEST, false)
    discoverSortOrderRatingCheck.visibleIf(sortOrder == RATING, false)
  }
}
