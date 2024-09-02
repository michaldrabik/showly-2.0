package com.michaldrabik.ui_progress.history.filters.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.HistoryPeriod
import com.michaldrabik.ui_progress.databinding.ViewHistoryPeriodFilterItemBinding

class HistoryPeriodItemView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewHistoryPeriodFilterItemBinding.inflate(LayoutInflater.from(context), this)

  var onItemClick: ((HistoryPeriod) -> Unit)? = null

  lateinit var item: HistoryPeriod

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    val paddingHorizontal = context.dimenToPx(R.dimen.spaceNormal)
    setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
    addRipple()
    onClick(safe = false) { onItemClick?.invoke(item) }
  }

  fun bind(
    item: HistoryPeriod,
    isChecked: Boolean,
  ) {
    this.item = item
    with(binding) {
      badge.visibleIf(isChecked)
      with(nameText) {
        val color = if (isChecked) android.R.attr.textColorPrimary else android.R.attr.textColorSecondary
        val typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        setTextColor(context.colorFromAttr(color))
        setTypeface(typeface)
        text = context.getString(item.displayStringRes)
      }
    }
  }
}
