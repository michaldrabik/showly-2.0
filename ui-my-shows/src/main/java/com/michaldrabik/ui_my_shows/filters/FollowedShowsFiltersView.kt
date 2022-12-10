package com.michaldrabik.ui_my_shows.filters

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.SortType.ASCENDING
import com.michaldrabik.ui_model.SortType.DESCENDING
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.databinding.ViewShowsFiltersBinding

class FollowedShowsFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewShowsFiltersBinding.inflate(LayoutInflater.from(context), this)

  var onSortChipClicked: ((SortOrder, SortType) -> Unit)? = null
  var onFilterUpcomingClicked: ((Boolean) -> Unit)? = null
  var onListViewModeClicked: (() -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  var isUpcomingChipVisible: Boolean
    get() = binding.followedShowsUpcomingChip.visibility == VISIBLE
    set(value) {
      binding.followedShowsUpcomingChip.visibleIf(value)
    }

  fun bind(
    sortOrder: SortOrder,
    sortType: SortType,
    isUpcoming: Boolean = false,
  ) {
    with(binding) {
      val sortIcon = when (sortType) {
        ASCENDING -> R.drawable.ic_arrow_alt_up
        DESCENDING -> R.drawable.ic_arrow_alt_down
      }
      followedShowsSortingChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      followedShowsSortingChip.text = context.getText(sortOrder.displayString)
      followedShowsUpcomingChip.isChecked = isUpcoming

      followedShowsSortingChip.onClick { onSortChipClicked?.invoke(sortOrder, sortType) }
      followedShowsUpcomingChip.onClick { onFilterUpcomingClicked?.invoke(followedShowsUpcomingChip.isChecked) }
      followedShowsListViewChip.onClick { onListViewModeClicked?.invoke() }
    }
  }
}
