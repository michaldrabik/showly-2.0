package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_sort_orded.view.*

class SortOrderView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  var sortSelectedListener: (SortOrder) -> Unit = {}

  init {
    inflate(context, R.layout.view_sort_orded, this)
    orientation = VERTICAL
    setBackgroundResource(R.color.colorSortViewBackground)

    sortOrderName.onClick { sortSelectedListener(SortOrder.NAME) }
    sortOrderNewest.onClick { sortSelectedListener(SortOrder.NEWEST) }
    sortOrderRating.onClick { sortSelectedListener(SortOrder.RATING) }
  }
}