package com.michaldrabik.showly2.ui.followedshows.statistics.views.mostWatched

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.ShowView
import kotlinx.android.synthetic.main.view_statistics_most_watched_item.view.*

@SuppressLint("SetTextI18n")
class StatisticsMostWatchedItemView : ShowView<StatisticsMostWatchedItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_most_watched_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  override val imageView: ImageView = viewMostWatchedItemImage
  override val placeholderView: ImageView = viewMostWatchedItemPlaceholder

  private lateinit var item: StatisticsMostWatchedItem

  fun bind(item: StatisticsMostWatchedItem) {
    this.item = item
    clear()
    viewMostWatchedItemTitle.text = item.show.title
    loadImage(item) { _, _ -> }
  }

  private fun clear() {
    Glide.with(this).clear(viewMostWatchedItemImage)
  }
}
