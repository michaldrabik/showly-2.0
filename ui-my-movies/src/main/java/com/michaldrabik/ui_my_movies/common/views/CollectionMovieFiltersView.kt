package com.michaldrabik.ui_my_movies.common.views

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
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_movies.databinding.ViewMoviesFiltersBinding

class CollectionMovieFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMoviesFiltersBinding.inflate(LayoutInflater.from(context), this)

  var onSortChipClicked: ((SortOrder, SortType) -> Unit)? = null
  var onFilterUpcomingClicked: ((Boolean) -> Unit)? = null
  var onListViewModeClicked: (() -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  var isUpcomingChipVisible: Boolean
    get() = binding.followedMoviesUpcomingChip.visibility == VISIBLE
    set(value) {
      binding.followedMoviesUpcomingChip.visibleIf(value)
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
      followedMoviesSortingChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      followedMoviesSortingChip.text = context.getText(item.sortOrder.displayString)
      followedMoviesUpcomingChip.isChecked = item.isUpcoming
      followedMoviesListViewChip.setChipIconResource(
        when (viewMode) {
          LIST_NORMAL, LIST_COMPACT -> R.drawable.ic_view_list
          GRID, GRID_TITLE -> R.drawable.ic_view_grid
        }
      )

      followedMoviesSortingChip.onClick { onSortChipClicked?.invoke(item.sortOrder, item.sortType) }
      followedMoviesUpcomingChip.onClick { onFilterUpcomingClicked?.invoke(followedMoviesUpcomingChip.isChecked) }
      followedMoviesListViewChip.onClick { onListViewModeClicked?.invoke() }
    }
  }

  private fun bindMargins(viewMode: ListViewMode) {
    with(binding) {
      when (viewMode) {
        GRID, GRID_TITLE -> {
          followedMoviesScroll.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.collectionFiltersPaddingHorizontal),
            right = resources.getDimensionPixelSize(R.dimen.collectionFiltersPaddingHorizontal),
            bottom = resources.getDimensionPixelSize(R.dimen.collectionFiltersPaddingBottom)
          )
        }
        LIST_NORMAL, LIST_COMPACT -> {
          followedMoviesScroll.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            right = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            bottom = 0
          )
        }
      }
    }
  }
}
