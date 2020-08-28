package com.michaldrabik.showly2.ui.followedshows.statistics.views.ratings

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.followedshows.statistics.views.ratings.recycler.StatisticsRatingItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_statistics_rate_item.view.*

class StatisticsRateItemView : ShowView<StatisticsRatingItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_rate_item, this)
    val width = context.dimenToPx(R.dimen.statisticsRatingItemWidth)
    layoutParams = LayoutParams(width, WRAP_CONTENT)
    viewRateItemImageLayout.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = viewRateItemImage
  override val placeholderView: ImageView = viewRateItemPlaceholder

  private lateinit var item: StatisticsRatingItem

  fun bind(item: StatisticsRatingItem) {
    this.item = item
    clear()
    viewRateItemTitle.text = item.show.title
    viewRateItemRating.text = item.rating.rating.toString()
    loadImage(item) { _, _ -> }
  }

  private fun clear() {
    viewRateItemTitle.gone()
    viewRateItemPlaceholder.gone()
    Glide.with(this).clear(viewRateItemImage)
  }
}
