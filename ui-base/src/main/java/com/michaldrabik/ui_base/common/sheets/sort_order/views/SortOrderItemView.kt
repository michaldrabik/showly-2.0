package com.michaldrabik.ui_base.common.sheets.sort_order.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import kotlinx.android.synthetic.main.view_sort_order_item.view.*

class SortOrderItemView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onItemClickListener: ((SortOrder, SortType) -> Unit)? = null

  lateinit var sortOrder: SortOrder
  lateinit var sortType: SortType
  var isChecked: Boolean = false

  init {
    inflate(context, R.layout.view_sort_order_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    val paddingHorizontal = context.dimenToPx(R.dimen.spaceNormal)
    setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
    addRipple()
    onClick(safe = false) { onItemClickListener?.invoke(sortOrder, sortType) }
  }

  fun bind(
    sortOrder: SortOrder,
    sortType: SortType,
    isChecked: Boolean,
    animate: Boolean = false
  ) {
    this.sortOrder = sortOrder
    this.sortType = sortType
    this.isChecked = isChecked

    viewSortOrderItemBadge.visibleIf(isChecked)

    with(viewSortOrderItemTitle) {
      val color = if (isChecked) android.R.attr.textColorPrimary else android.R.attr.textColorSecondary
      val typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
      setTextColor(context.colorFromAttr(color))
      setTypeface(typeface)
      text = context.getString(sortOrder.displayString)
    }

    with(viewSortOrderItemAscDesc) {
      visibleIf(isChecked)
      val rotation = if (sortType == SortType.ASCENDING) -90F else 90F
      val duration = if (animate) 200L else 0
      animate().rotation(rotation).setDuration(duration).start()
    }
  }
}
