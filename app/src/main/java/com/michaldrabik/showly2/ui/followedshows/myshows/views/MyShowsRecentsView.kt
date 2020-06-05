package com.michaldrabik.showly2.ui.followedshows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.GridLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.screenWidth
import kotlinx.android.synthetic.main.view_my_shows_recents.view.*

class MyShowsRecentsView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_shows_recents, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  private val itemMargin by lazy { context.dimenToPx(R.dimen.spaceTiny) }
  private val itemHeight by lazy { context.dimenToPx(R.dimen.myShowsFanartHeight) }
  private val itemWidth by lazy { (screenWidth() - context.dimenToPx(R.dimen.spaceNormal) - (4 * itemMargin)) / 2 }

  fun bind(
    item: MyShowsItem.RecentsSection,
    itemClickListener: (MyShowsItem) -> Unit
  ) {
    myShowsRecentsContainer.removeAllViews()

    val clickListener: (MyShowsItem) -> Unit = { itemClickListener.invoke(it) }

    item.items.forEachIndexed { index, showItem ->
      val view = MyShowFanartView(context).apply {
        layoutParams = LayoutParams(0, MATCH_PARENT)
        bind(showItem, clickListener)
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = itemWidth
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      myShowsRecentsContainer.addView(view, layoutParams)
    }
  }
}
