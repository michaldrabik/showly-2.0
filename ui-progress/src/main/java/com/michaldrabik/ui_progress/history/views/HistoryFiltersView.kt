package com.michaldrabik.ui_progress.history.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.HistoryPeriod
import com.michaldrabik.ui_progress.databinding.ViewHistoryFiltersBinding
import com.michaldrabik.ui_progress.history.entities.HistoryListItem

internal class HistoryFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewHistoryFiltersBinding.inflate(LayoutInflater.from(context), this)

  var onDatesChipClick: ((HistoryPeriod) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      datesRangeChip.onClick {
        onDatesChipClick?.invoke(filters.period)
      }
    }
  }

  private lateinit var filters: HistoryListItem.Filters

  fun bind(filters: HistoryListItem.Filters) {
    this.filters = filters
    with(binding) {
      datesRangeChip.text = context.getText(filters.period.displayStringRes)
    }
  }
}
