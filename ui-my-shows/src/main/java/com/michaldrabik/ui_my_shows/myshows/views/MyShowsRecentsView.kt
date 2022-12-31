package com.michaldrabik.ui_my_shows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.core.view.updatePadding
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.LIST_COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import kotlinx.android.synthetic.main.view_my_shows_recents.view.*

class MyShowsRecentsView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_shows_recents, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
  }

  private val itemMargin by lazy { context.dimenToPx(R.dimen.spaceTiny) }
  private val itemHeight by lazy { context.dimenToPx(R.dimen.myShowsFanartHeight) }
  private val itemWidth by lazy { (screenWidth() - context.dimenToPx(R.dimen.spaceNormal) - (4 * itemMargin)) / 2 }

  fun bind(
    item: MyShowsItem.RecentsSection,
    viewMode: ListViewMode,
    itemClickListener: ((MyShowsItem) -> Unit)?,
    itemLongClickListener: ((MyShowsItem) -> Unit)?,
  ) {
    myShowsRecentsContainer.removeAllViews()

    val clickListener: (MyShowsItem) -> Unit = { itemClickListener?.invoke(it) }
    val longClickListener: (MyShowsItem, View) -> Unit = { i, _ -> itemLongClickListener?.invoke(i) }

    item.items.forEachIndexed { index, showItem ->
      val view = MyShowFanartView(context).apply {
        layoutParams = LayoutParams(0, MATCH_PARENT)
        bind(showItem, clickListener, longClickListener)
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = itemWidth
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      myShowsRecentsContainer.addView(view, layoutParams)
    }

    bindMargins(viewMode)
  }

  private fun bindMargins(viewMode: ListViewMode) {
    when (viewMode) {
      GRID, GRID_TITLE -> {
        myShowsRecentsContainer.updatePadding(
          left = resources.getDimensionPixelSize(R.dimen.myShowsRecentsGridPadding),
          right = resources.getDimensionPixelSize(R.dimen.myShowsRecentsGridPadding),
        )
      }
      LIST_NORMAL, LIST_COMPACT -> {
        myShowsRecentsContainer.updatePadding(
          left = resources.getDimensionPixelSize(R.dimen.spaceSmall),
          right = resources.getDimensionPixelSize(R.dimen.spaceSmall),
        )
      }
    }
  }
}
