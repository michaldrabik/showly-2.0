package com.michaldrabik.ui_my_movies.mymovies.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.GridLayout
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.databinding.ViewMyMoviesRecentsBinding
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem

class MyMoviesRecentsView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMyMoviesRecentsBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
  }

  private val itemMargin by lazy { context.dimenToPx(R.dimen.spaceTiny) }
  private val itemHeight by lazy { context.dimenToPx(R.dimen.myMoviesFanartHeight) }
  private val itemWidth by lazy {
    val space = context.dimenToPx(R.dimen.screenMarginHorizontal) * 2
    ((screenWidth() - space) / 2) - itemMargin
  }

  fun bind(
    item: MyMoviesItem.RecentsSection,
    itemClickListener: ((MyMoviesItem) -> Unit)?,
    itemLongClickListener: ((MyMoviesItem) -> Unit)?,
  ) {
    binding.myMoviesRecentsContainer.removeAllViews()

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
        if (index % 2 == 0) {
          setMargins(0, itemMargin, itemMargin, itemMargin)
        } else {
          setMargins(itemMargin, itemMargin, 0, itemMargin)
        }
      }
      binding.myMoviesRecentsContainer.addView(view, layoutParams)
    }
  }
}
