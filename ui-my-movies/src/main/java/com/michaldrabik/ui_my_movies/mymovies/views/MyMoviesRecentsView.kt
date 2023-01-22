package com.michaldrabik.ui_my_movies.mymovies.views

import android.content.Context
import android.util.AttributeSet
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
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import kotlinx.android.synthetic.main.view_my_movies_recents.view.*

class MyMoviesRecentsView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_movies_recents, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
  }

  private val itemMargin by lazy { context.dimenToPx(R.dimen.spaceTiny) }
  private val itemHeight by lazy { context.dimenToPx(R.dimen.myMoviesFanartHeight) }
  private val itemWidth by lazy { (screenWidth() - context.dimenToPx(R.dimen.spaceNormal) - (4 * itemMargin)) / 2 }

  fun bind(
    item: MyMoviesItem.RecentsSection,
    viewMode: ListViewMode,
    itemClickListener: ((MyMoviesItem) -> Unit)?,
    itemLongClickListener: ((MyMoviesItem) -> Unit)?,
  ) {
    myMoviesRecentsContainer.removeAllViews()

    val clickListener: (MyMoviesItem) -> Unit = { itemClickListener?.invoke(it) }
    val longClickListener: (MyMoviesItem) -> Unit = { itemLongClickListener?.invoke(it) }

    item.items.forEachIndexed { index, showItem ->
      val view = MyMovieFanartView(context).apply {
        layoutParams = LayoutParams(0, MATCH_PARENT)
        bind(showItem, clickListener, longClickListener)
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = itemWidth
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      myMoviesRecentsContainer.addView(view, layoutParams)
    }

    bindMargins(viewMode)
  }

  private fun bindMargins(viewMode: ListViewMode) {
    when (viewMode) {
      GRID, GRID_TITLE -> {
        myMoviesRecentsContainer.updatePadding(
          left = resources.getDimensionPixelSize(R.dimen.myMoviesRecentsGridPadding),
          right = resources.getDimensionPixelSize(R.dimen.myMoviesRecentsGridPadding),
        )
      }
      LIST_NORMAL, LIST_COMPACT -> {
        myMoviesRecentsContainer.updatePadding(
          left = resources.getDimensionPixelSize(R.dimen.spaceSmall),
          right = resources.getDimensionPixelSize(R.dimen.spaceSmall),
        )
      }
    }
  }
}
