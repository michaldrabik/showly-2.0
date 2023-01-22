package com.michaldrabik.ui_my_movies.mymovies.views

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
import com.michaldrabik.ui_model.MyMoviesSection.ALL
import com.michaldrabik.ui_model.MyMoviesSection.RECENTS
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.databinding.ViewMyMoviesHeaderBinding
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import java.util.Locale.ENGLISH

class MyMovieHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMyMoviesHeaderBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
  }

  fun bind(
    item: MyMoviesItem.Header,
    viewMode: ListViewMode,
    sortClickListener: (SortOrder, SortType) -> Unit,
    listModeClickListener: (() -> Unit)?,
  ) {
    bindMargins(viewMode)
    bindLabel(item)
    with(binding) {
      myMoviesFilterChipsScroll.visibleIf(item.section == ALL)
      myMoviesSortChip.visibleIf(item.sortOrder != null)

      with(myMoviesSortListViewChip) {
        when (viewMode) {
          LIST_NORMAL, LIST_COMPACT -> setChipIconResource(R.drawable.ic_view_list)
          GRID, GRID_TITLE -> setChipIconResource(R.drawable.ic_view_grid)
        }
        onClick { listModeClickListener?.invoke() }
      }

      item.sortOrder?.let { sortOrder ->
        myMoviesSortChip.text = context.getString(sortOrder.first.displayString)
        myMoviesSortChip.onClick {
          sortClickListener.invoke(sortOrder.first, sortOrder.second)
        }
        val sortIcon = when (sortOrder.second) {
          SortType.ASCENDING -> R.drawable.ic_arrow_alt_up
          SortType.DESCENDING -> R.drawable.ic_arrow_alt_down
        }
        myMoviesSortChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      }
    }
  }

  private fun bindLabel(item: MyMoviesItem.Header) {
    val headerLabel = context.getString(item.section.displayString)
    binding.myMoviesHeaderLabel.text = when (item.section) {
      RECENTS -> headerLabel
      else -> String.format(ENGLISH, "%s (%d)", headerLabel, item.itemCount)
    }
  }

  private fun bindMargins(viewMode: ListViewMode) {
    with(binding) {
      when (viewMode) {
        GRID, GRID_TITLE -> {
          myMoviesFilterChipsScroll.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.myMoviesHeaderGridPadding),
            right = resources.getDimensionPixelSize(R.dimen.myMoviesHeaderGridPadding),
            bottom = resources.getDimensionPixelSize(R.dimen.spaceTiny)
          )
          myMoviesHeaderLabel.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.myMoviesHeaderGridPadding),
            right = resources.getDimensionPixelSize(R.dimen.myMoviesHeaderGridPadding),
          )
        }
        LIST_NORMAL, LIST_COMPACT -> {
          myMoviesFilterChipsScroll.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            right = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            bottom = 0
          )
          myMoviesHeaderLabel.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            right = resources.getDimensionPixelSize(R.dimen.spaceMedium)
          )
        }
      }
    }
  }
}
