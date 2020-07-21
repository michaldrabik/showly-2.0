package com.michaldrabik.showly2.ui.discover.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.DiscoverFilters
import com.michaldrabik.showly2.model.DiscoverSortOrder.HOT
import com.michaldrabik.showly2.model.DiscoverSortOrder.NEWEST
import com.michaldrabik.showly2.model.DiscoverSortOrder.RATING
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_discover_filters.view.*

class DiscoverFiltersView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onApplyClickListener: ((DiscoverFilters) -> Unit)? = null

  init {
    val spaceNormal = context.dimenToPx(R.dimen.spaceNormal)
    val spaceSmall = context.dimenToPx(R.dimen.spaceSmall)

    inflate(context, R.layout.view_discover_filters, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    isClickable = true
    clipChildren = false
    clipToPadding = false
    setBackgroundResource(R.drawable.bg_discover_filters)
    setPadding(spaceNormal, spaceNormal, spaceNormal, spaceSmall)

    discoverFiltersApplyButton.onClick { onApplyFilters() }
  }

  fun bind(filters: DiscoverFilters) {
    discoverFiltersChipHot.isChecked = filters.sortOrder == HOT
    discoverFiltersChipTopRated.isChecked = filters.sortOrder == RATING
    discoverFiltersChipMostRecent.isChecked = filters.sortOrder == NEWEST
  }

  private fun onApplyFilters() {
    val sortOrder = when {
      discoverFiltersChipHot.isChecked -> HOT
      discoverFiltersChipTopRated.isChecked -> RATING
      discoverFiltersChipMostRecent.isChecked -> NEWEST
      else -> throw IllegalStateException()
    }

    val filters = DiscoverFilters(sortOrder, true)
    onApplyClickListener?.invoke(filters)
  }
}
