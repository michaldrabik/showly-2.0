package com.michaldrabik.ui_progress.progress.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortType.ASCENDING
import com.michaldrabik.ui_model.SortType.DESCENDING
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.databinding.ViewProgressFiltersBinding
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem

class ProgressFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewProgressFiltersBinding.inflate(LayoutInflater.from(context), this)

  var onSortChipClicked: (() -> Unit)? = null
  var upcomingChipClicked: ((Boolean) -> Unit)? = null
  var onHoldChipClicked: ((Boolean) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      progressFiltersSortingChip.onClick {
        onSortChipClicked?.invoke()
      }
      progressFiltersUpcomingChip.onClick(safe = false) {
        if (!::filters.isInitialized) return@onClick
        upcomingChipClicked?.invoke(!filters.isUpcoming)
      }
      progressFiltersOnHoldChip.onClick(safe = false) {
        if (!::filters.isInitialized) return@onClick
        onHoldChipClicked?.invoke(!filters.isOnHold)
      }
    }
  }

  private lateinit var filters: ProgressListItem.Filters

  fun bind(filters: ProgressListItem.Filters) {
    this.filters = filters
    with(binding) {
      val sortIcon = when (filters.sortType) {
        ASCENDING -> R.drawable.ic_arrow_alt_up
        DESCENDING -> R.drawable.ic_arrow_alt_down
      }
      progressFiltersSortingChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      progressFiltersSortingChip.text = context.getText(filters.sortOrder.displayString)
      progressFiltersUpcomingChip.visibleIf(filters.isUpcomingEnabled)
      progressFiltersUpcomingChip.isSelected = filters.isUpcoming
      progressFiltersOnHoldChip.isSelected = filters.isOnHold
    }
  }
}
