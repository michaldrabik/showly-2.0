package com.michaldrabik.ui_statistics.views.mostWatched

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_statistics.R
import kotlinx.android.synthetic.main.view_statistics_most_watched_item.view.*

@SuppressLint("SetTextI18n")
class StatisticsMostWatchedItemView : ShowView<StatisticsMostWatchedItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_most_watched_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    viewMostWatchedItem.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = viewMostWatchedItemImage
  override val placeholderView: ImageView = viewMostWatchedItemPlaceholder

  private lateinit var item: StatisticsMostWatchedItem

  override fun bind(item: StatisticsMostWatchedItem) {
    this.item = item
    clear()

    viewMostWatchedItemTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.show.title
      else item.translation?.title
    viewMostWatchedItemHoursValue.text = "${item.episodes.sumOf { it.runtime } / 60}"
    viewMostWatchedItemEpisodesValue.text = "${item.episodes.size}"
    viewMostWatchedItemSeasonsValue.text = "${item.seasonsCount}"
    loadImage(item)
  }

  private fun clear() {
    Glide.with(this).clear(viewMostWatchedItemImage)
  }
}
