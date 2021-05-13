package com.michaldrabik.ui_my_movies.mymovies.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.GridLayout
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
    itemClickListener: ((MyMoviesItem) -> Unit)?
  ) {
    myMoviesRecentsContainer.removeAllViews()

    val clickListener: (MyMoviesItem) -> Unit = { itemClickListener?.invoke(it) }

    item.items.forEachIndexed { index, showItem ->
      val view = MyMovieFanartView(context).apply {
        layoutParams = LayoutParams(0, MATCH_PARENT)
        bind(showItem, clickListener)
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = itemWidth
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      myMoviesRecentsContainer.addView(view, layoutParams)
    }
  }
}
