package com.michaldrabik.ui_my_shows.myshows.views

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
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.databinding.ViewMyShowsHeaderBinding
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import java.util.Locale.ENGLISH

class MyShowHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMyShowsHeaderBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
  }

  fun bind(
    item: MyShowsItem.Header,
    viewMode: ListViewMode,
    typeClickListener: (() -> Unit)?,
    sortClickListener: ((MyShowsSection, SortOrder, SortType) -> Unit)?,
    listModeClickListener: (() -> Unit)?,
  ) {
    bindMargins(viewMode)
    bindLabel(item)
    with(binding) {
      myShowsFilterChipsScroll.visibleIf(item.section != RECENTS)
      myShowsSortChip.visibleIf(item.sortOrder != null)

      with(myShowsTypeChip) {
        isSelected = true
        text = context.getString(item.section.displayString)
        visibleIf(item.section != RECENTS)
        onClick { typeClickListener?.invoke() }
      }

      with(myShowsSortListViewChip) {
        when (viewMode) {
          LIST_NORMAL, LIST_COMPACT -> setChipIconResource(R.drawable.ic_view_list)
          GRID, GRID_TITLE -> setChipIconResource(R.drawable.ic_view_grid)
        }
        onClick { listModeClickListener?.invoke() }
      }

      item.sortOrder?.let { sortOrder ->
        myShowsSortChip.text = context.getString(sortOrder.first.displayString)
        myShowsSortChip.onClick {
          sortClickListener?.invoke(item.section, sortOrder.first, sortOrder.second)
        }
        val sortIcon = when (sortOrder.second) {
          SortType.ASCENDING -> R.drawable.ic_arrow_alt_up
          SortType.DESCENDING -> R.drawable.ic_arrow_alt_down
        }
        myShowsSortChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      }
    }
  }

  private fun bindMargins(viewMode: ListViewMode) {
    with(binding) {
      when (viewMode) {
        GRID, GRID_TITLE -> {
          myShowsFilterChipsScroll.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.myShowsHeaderGridPadding),
            right = resources.getDimensionPixelSize(R.dimen.myShowsHeaderGridPadding),
            bottom = resources.getDimensionPixelSize(R.dimen.spaceTiny)
          )
          myShowsHeaderLabel.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.myShowsHeaderGridPadding),
            right = resources.getDimensionPixelSize(R.dimen.myShowsHeaderGridPadding),
          )
        }
        LIST_NORMAL, LIST_COMPACT -> {
          myShowsFilterChipsScroll.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            right = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            bottom = 0
          )
          myShowsHeaderLabel.updatePadding(
            left = resources.getDimensionPixelSize(R.dimen.spaceMedium),
            right = resources.getDimensionPixelSize(R.dimen.spaceMedium)
          )
        }
      }
    }
  }

  private fun bindLabel(item: MyShowsItem.Header) {
    val headerLabel = context.getString(item.section.displayString)
    binding.myShowsHeaderLabel.text = when (item.section) {
      RECENTS -> headerLabel
      else -> String.format(ENGLISH, "%s (%d)", headerLabel, item.itemCount)
    }
  }
}
