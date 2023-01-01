package com.michaldrabik.ui_my_shows.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.LIST_COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.SortType.ASCENDING
import com.michaldrabik.ui_model.SortType.DESCENDING
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_shows.databinding.ViewShowsFiltersBinding

class CollectionShowFiltersView : FrameLayout {

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
    item: CollectionListItem.FiltersItem,
    viewMode: ListViewMode,
  ) {
    bindMargins(viewMode)
    with(binding) {
      val sortIcon = when (item.sortType) {
        ASCENDING -> R.drawable.ic_arrow_alt_up
        DESCENDING -> R.drawable.ic_arrow_alt_down
      }
      followedShowsSortingChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      followedShowsSortingChip.text = context.getText(item.sortOrder.displayString)
      followedShowsUpcomingChip.isChecked = item.isUpcoming
      followedShowsListViewChip.setChipIconResource(
        when (viewMode) {
          LIST_NORMAL, LIST_COMPACT -> R.drawable.ic_view_list
          GRID, GRID_TITLE -> R.drawable.ic_view_grid
        }
      )

      followedShowsSortingChip.onClick { onSortChipClicked?.invoke(item.sortOrder, item.sortType) }
      followedShowsUpcomingChip.onClick { onFilterUpcomingClicked?.invoke(followedShowsUpcomingChip.isChecked) }
      followedShowsListViewChip.onClick { onListViewModeClicked?.invoke() }
    }
  }

  private fun bindMargins(viewMode: ListViewMode) {
    with(binding) {
      when (viewMode) {
        GRID, GRID_TITLE -> {
          followedShowsScroll.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.collectionFiltersPaddingHorizontal),
            right = resources.getDimensionPixelSize(R.dimen.collectionFiltersPaddingHorizontal),
            bottom = resources.getDimensionPixelSize(R.dimen.collectionFiltersPaddingBottom)
          )
        }
        LIST_NORMAL, LIST_COMPACT -> {
          followedShowsScroll.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            right = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            bottom = 0
          )
        }
      }
    }
  }
}
